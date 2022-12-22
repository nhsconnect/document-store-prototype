package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;
import uk.nhs.digital.docstore.services.DocumentReferenceService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateDocumentReferenceInlineTest {
    @Mock
    private Context context;
    @Mock
    private AmazonSQS amazonSqsClient;
    @Mock
    private DynamoDBMapper dynamoDBMapper;
    @Mock
    private AmazonS3 s3Client;

    private CreateDocumentReferenceHandler handler;
    private RequestEventBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        handler = new CreateDocumentReferenceHandler(new StubbedApiConfig("http://ui-url"),
                new DocumentReferenceService(new DocumentMetadataStore(dynamoDBMapper), new SplunkPublisher(amazonSqsClient)),
                s3Client);
        requestBuilder = new RequestEventBuilder();
    }

    @Test
    void shouldSuccessfullyCreateDocumentReference() throws IOException {
        var requestContent = getContentFromResource("create/CreateDocumentReferenceRequest.json");

        when(s3Client.generatePresignedUrl(any())).thenReturn(new URL("http://presigned-url"));
        doNothing().when(dynamoDBMapper).save(any());

        var request = requestBuilder
                .addBody(requestContent)
                .build();
        var responseEvent = handler.handleRequest(request, context);
        var id = JsonPath.read(responseEvent.getBody(), "$.id");

        assertThat(responseEvent.getStatusCode()).isEqualTo(201);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThat(responseEvent.getHeaders().get("Location")).isEqualTo("DocumentReference/" + id);
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.id", "$.meta")
                .isEqualTo(getContentFromResource("create/CreatedDocumentReference.json"));
    }

    @Test
    void returnsBadRequestIfCodingSystemIsNotSupported() throws IOException {
        var expectedErrorResponse = getContentFromResource("create/unsupported-coding-system-response.json");
        var requestContent = getContentFromResource("create/unsupported-coding-system-request.json");

        var request = requestBuilder
                .addBody(requestContent)
                .build();
        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
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