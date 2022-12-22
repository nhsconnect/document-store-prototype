package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;
import uk.nhs.digital.docstore.services.DocumentReferenceService;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
public class CreateDocumentReferenceInlineTest {
    @Mock
    private Context context;
    @Mock
    private AmazonSQS amazonSqsClient;
    @Mock
    private DynamoDBMapper dynamoDBMapper;
    @Mock
    private AmazonS3 s3Client;

    @Captor
    ArgumentCaptor<SendMessageRequest> messageRequestCaptor;

    @SystemStub
    private EnvironmentVariables environmentVariables;

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
    void createsDocumentReference() throws IOException {
        var requestContent = getContentFromResource("create/create-document-reference-request.json");
        var request = requestBuilder.addBody(requestContent).build();

        when(s3Client.generatePresignedUrl(any())).thenReturn(new URL("http://presigned-url"));
        doNothing().when(dynamoDBMapper).save(any());
        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(201);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        var id = JsonPath.read(responseEvent.getBody(), "$.id");
        assertThat(responseEvent.getHeaders().get("Location")).isEqualTo("DocumentReference/" + id);
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.id", "$.meta")
                .isEqualTo(getContentFromResource("create/created-document-reference.json"));
    }

    @Test
    void returnsBadRequestIfCodingSystemIsNotSupported() throws IOException {
        var expectedErrorResponse = getContentFromResource("create/unsupported-coding-system-response.json");
        var requestContent = getContentFromResource("create/unsupported-coding-system-request.json");
        var request = requestBuilder.addBody(requestContent).build();
        var responseEvent = handler.handleRequest(request, context);

        verify(amazonSqsClient, never()).sendMessage(any());
        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody()).isEqualTo(expectedErrorResponse);
    }

    @Test
    void sendsAuditMessageToSqsWhenDocumentReferenceSuccessfullyCreated() throws IOException {
        var requestContent = getContentFromResource("create/create-document-reference-request.json");
        var request = requestBuilder.addBody(requestContent).build();
        var expectedMessageBody = new JSONObject();
        expectedMessageBody.put("nhsNumber", "34567");
        expectedMessageBody.put("fileName", "uploaded document");
        expectedMessageBody.put("fileType", "text/plain");
        expectedMessageBody.put("created", "2021-11-03T15:57:30Z");

        environmentVariables.set("SQS_QUEUE_URL", "document-store-audit-queue-url");
        when(s3Client.generatePresignedUrl(any())).thenReturn(new URL("http://presigned-url"));
        handler.handleRequest(request, context);

        verify(amazonSqsClient).sendMessage(messageRequestCaptor.capture());
        var messageBody = messageRequestCaptor.getValue().getMessageBody();
        assertThatJson(messageBody).whenIgnoringPaths("id").isEqualTo(expectedMessageBody);
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