package uk.nhs.digital.docstore;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.handlers.CreateDocumentReferenceHandler;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;
import uk.nhs.digital.docstore.helpers.BaseUriHelper;
import uk.nhs.digital.docstore.services.DocumentReferenceService;

@ExtendWith(MockitoExtension.class)
public class CreateDocumentReferenceInlineTest {
    private static final String AWS_REGION = "eu-west-2";
    @Mock private Context context;
    @Mock private AuditPublisher auditPublisher;
    private CreateDocumentReferenceHandler handler;
    private RequestEventBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        var endpoint = String.format("http://%s:4566", BaseUriHelper.getAwsHost());

        var endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);
        var dynamodbClient =
                AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .build();
        var dynamoDBMapper = new DynamoDBMapper(dynamodbClient);

        var s3Client =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .withPathStyleAccessEnabled(true)
                        .build();
        var bucketName = new AwsS3Helper(endpointConfiguration).getDocumentStoreBucketName();

        handler =
                new CreateDocumentReferenceHandler(
                        new StubbedApiConfig("http://ui-url"),
                        new DocumentReferenceService(
                                new DocumentMetadataStore(dynamoDBMapper),
                                auditPublisher,
                                new DocumentMetadataSerialiser()),
                        new DocumentStore(s3Client, bucketName));
        requestBuilder = new RequestEventBuilder();
    }

    @Test
    void createsDocumentReference() throws IOException {
        var requestContent =
                getContentFromResource("create/create-document-reference-request.json");
        var request = requestBuilder.addBody(requestContent).build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(201);
        assertThat(responseEvent.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        var id = JsonPath.read(responseEvent.getBody(), "$.id");
        var presignedUrl = JsonPath.read(responseEvent.getBody(), "$.content[0].attachment.url");
        assertThat(responseEvent.getHeaders().get("Location")).isEqualTo("DocumentReference/" + id);
        assertThat(presignedUrl).isNotNull();
        assertDoesNotThrow(
                () -> {
                    new URL(presignedUrl.toString());
                });
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.id", "$.meta", "$.content[*].attachment.url")
                .isEqualTo(getContentFromResource("create/created-document-reference.json"));

        verify(auditPublisher).publish(any(CreateDocumentMetadataAuditMessage.class));
    }

    @Test
    void returnsBadRequestIfCodingSystemIsNotSupported() throws IOException {
        var expectedErrorResponse =
                getContentFromResource("create/unsupported-coding-system-response.json");
        var requestContent =
                getContentFromResource("create/unsupported-coding-system-request.json");
        var request = requestBuilder.addBody(requestContent).build();
        var responseEvent = handler.handleRequest(request, context);

        verify(auditPublisher, never()).publish(any());
        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody()).isEqualTo(expectedErrorResponse);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static class RequestEventBuilder {
        private String body;

        private RequestEventBuilder addBody(String value) {
            body = value;
            return this;
        }

        private APIGatewayProxyRequestEvent build() {
            return new APIGatewayProxyRequestEvent().withBody(body);
        }
    }
}
