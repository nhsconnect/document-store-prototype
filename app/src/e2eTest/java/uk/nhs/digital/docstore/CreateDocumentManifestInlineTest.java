package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.documentmanifest.CreateDocumentManifestByNhsNumberHandler;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.services.DocumentManifestService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static com.auth0.jwt.algorithms.Algorithm.none;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateDocumentManifestInlineTest {
    @Mock
    private Context context;
    @Mock
    private DynamoDBMapper dynamoDbMapper;
    @Mock
    private AmazonS3 s3Client;
    @Mock
    private AuditPublisher auditPublisher;
    @Mock
    private DocumentMetadataStore metadataStore;

    private CreateDocumentManifestByNhsNumberHandler createDocumentManifestByNhsNumberHandler;

    @BeforeEach
    public void setUp() {
        createDocumentManifestByNhsNumberHandler = new CreateDocumentManifestByNhsNumberHandler(
                new StubbedApiConfig("http://ui-url"),
                metadataStore,
                new DocumentZipTraceStore(dynamoDbMapper),
                new DocumentStore(s3Client, "bucket-name"),
                new DocumentManifestService(auditPublisher),
                "1"
        );
    }

    @Test
    void uploadsZipOfAllDocsAndSavesMetadataForGivenNhsNumber() throws MalformedURLException {
        var nhsNumber = "9000000009";
        var presignedUrl = "http://presigned-url";
        var metadataList = List.of(createMetadata("some document"), createMetadata("another document"));
        var requestEvent = createRequestEvent(nhsNumber);

        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(metadataList);
        when(s3Client.getObject(anyString(), anyString())).thenReturn(new S3Object());
        when(s3Client.generatePresignedUrl(any())).thenReturn(new URL(presignedUrl));
        doNothing().when(dynamoDbMapper).save(any());
        var responseEvent = createDocumentManifestByNhsNumberHandler.handleRequest(requestEvent, context);

        var responseUrl = JsonPath.<String>read(responseEvent.getBody(), "$.result.url");
        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThat(responseUrl).isEqualTo(presignedUrl);
    }

    @Test
    void sendsAuditMessageAfterZipIsUploadedSuccessfully() throws MalformedURLException, JsonProcessingException {
        var nhsNumber = "9000000009";
        var presignedUrl = "http://presigned-url";
        var requestEvent = createRequestEvent(nhsNumber);
        var metadataList = List.of(createMetadata("some document"));

        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(metadataList);
        when(s3Client.getObject(anyString(), anyString())).thenReturn(new S3Object());
        when(s3Client.generatePresignedUrl(any())).thenReturn(new URL(presignedUrl));
        doNothing().when(dynamoDbMapper).save(any());
        createDocumentManifestByNhsNumberHandler.handleRequest(requestEvent, context);

        verify(auditPublisher).publish(any(DownloadAllPatientRecordsAuditMessage.class));
    }

    private APIGatewayProxyRequestEvent createRequestEvent(String nhsNumber) {
        HashMap<String, String> headers = new HashMap<>();
        HashMap<String, String> parameters = new HashMap<>();
        headers.put("Authorization", "Bearer " + JWT.create().withClaim("email", "").sign(none()));
        parameters.put("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber);

        return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters).withHeaders(headers);
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
}