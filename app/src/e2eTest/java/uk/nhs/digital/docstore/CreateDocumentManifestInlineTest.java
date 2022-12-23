package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.auth0.jwt.JWT;
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
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.documentmanifest.CreateDocumentManifestByNhsNumberHandler;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;
import uk.nhs.digital.docstore.services.DocumentManifestService;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;

import static com.auth0.jwt.algorithms.Algorithm.none;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CreateDocumentManifestInlineTest {

    @Mock
    private Context context;
    @Mock
    private DynamoDBMapper dynamoDBMapper;
    @Mock
    private AmazonS3 s3Client;
    @Mock
    private DocumentMetadataStore metadataStore;
    @Mock
    private AmazonSQSClient sqsClient;
    @Captor
    ArgumentCaptor<SendMessageRequest> messageRequestCaptor;
    private CreateDocumentManifestByNhsNumberHandler handler;
    private RequestEventBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        handler = new CreateDocumentManifestByNhsNumberHandler(new StubbedApiConfig("http://ui-url"),
                metadataStore,
                new DocumentZipTraceStore(dynamoDBMapper),
                new DocumentStore(s3Client, "bucket-name"),
                new DocumentManifestService(new SplunkPublisher(sqsClient)),
                "1");
        requestBuilder = new RequestEventBuilder();
    }

    @Test
    void shouldUploadZipOfAllDocumentsAndSaveMetadataForGivenNhsNumber() throws MalformedURLException {
        var nhsNumber = "9000000009";
        var presignedUrl = "http://presigned-url";

        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(
                List.of(createMetadata("some document"), createMetadata("another document")));
        when(s3Client.getObject(anyString(), anyString())).thenReturn(new S3Object());
        when(s3Client.generatePresignedUrl(any())).thenReturn(new URL(presignedUrl));
        doNothing().when(dynamoDBMapper).save(any());

        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber)
                .addHeader("Authorization", createBearerToken())
                .build();
        var responseEvent = handler.handleRequest(request, context);
        var responseUrl =  JsonPath.<String>read(responseEvent.getBody(), "$.result.url");

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThat(responseUrl).isEqualTo(presignedUrl);
    }

    @Test
    void shouldSendAuditMessageAfterZipIsUploadedSuccessfully() throws MalformedURLException {
        var nhsNumber = "9000000009";
        var presignedUrl = "http://presigned-url";
        var now = Instant.now();

        var fileMetadata = new JSONObject();
        fileMetadata.put("id","123");
        fileMetadata.put("fileName","some document");
        fileMetadata.put("fileType","text/plain");

        var expectedMessageBody = new JSONObject();
        expectedMessageBody.put("nhsNumber", nhsNumber);
        expectedMessageBody.put("fileMetadataList", List.of(fileMetadata));
        expectedMessageBody.put("timestamp", now.toString());

        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber)
                .addHeader("Authorization", createBearerToken())
                .build();

        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(
                List.of(createMetadata("some document")));
        when(s3Client.getObject(anyString(), anyString())).thenReturn(new S3Object());
        when(s3Client.generatePresignedUrl(any())).thenReturn(new URL(presignedUrl));
        doNothing().when(dynamoDBMapper).save(any());

        handler.handleRequest(request, context);

        verify(sqsClient).sendMessage(messageRequestCaptor.capture());
        var messageBody = messageRequestCaptor.getValue().getMessageBody();
        var timestamp = JsonPath.read(messageBody, "$.timestamp").toString();
        assertThatJson(messageBody).whenIgnoringPaths("fileMetadata.id", "timestamp").isEqualTo(expectedMessageBody);
        assertThat(Instant.parse(timestamp)).isCloseTo(now, within(1, ChronoUnit.SECONDS));
    }

    private DocumentMetadata createMetadata(String fileName) {
        var metadata = new DocumentMetadata();
        metadata.setId("123");
        metadata.setDocumentUploaded(true);
        metadata.setDescription(fileName);
        metadata.setContentType("text/plain");
        metadata.setLocation("s3://bucket/key");
        return metadata;
    }

    public static class RequestEventBuilder {
        private HashMap<String, String> parameters = new HashMap<>();
        private HashMap<String, String> headers = new HashMap<>();

        private RequestEventBuilder addQueryParameter(String name, String value) {
            parameters.put(name, value);
            return this;
        }

        private RequestEventBuilder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        private APIGatewayProxyRequestEvent build() {
            return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters).withHeaders(headers);
        }
    }

    private String createBearerToken() {
        String jwt = JWT.create()
                .withClaim("email", "")
                .sign(none());
        return "Bearer " + jwt;
    }
}