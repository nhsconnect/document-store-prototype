package uk.nhs.digital.docstore;

import static com.auth0.jwt.algorithms.Algorithm.none;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.lambdas.CreateDocumentManifestByNhsNumberHandler;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.NhsNumber;

@ExtendWith(MockitoExtension.class)
public class CreateDocumentManifestTest extends BaseDocumentStoreTest {
    @Mock private Context context;

    @Mock private SplunkPublisher publisher;

    private DocumentMetadataStore metadataStore;

    private DocumentStore documentStore;

    private CreateDocumentManifestByNhsNumberHandler createDocumentManifestByNhsNumberHandler;

    @BeforeEach
    public void setUp() {
        metadataStore = new DocumentMetadataStore(new DynamoDBMapper(aws.getDynamoDBClient()));
        var workspace = System.getenv("WORKSPACE");
        System.out.println(workspace);
        documentStore = new DocumentStore(aws.getS3Client());
        DocumentZipTraceStore zipTraceStore =
                new DocumentZipTraceStore(new DynamoDBMapper(aws.getDynamoDBClient()));
        createDocumentManifestByNhsNumberHandler =
                new CreateDocumentManifestByNhsNumberHandler(
                        new StubbedApiConfig("http://ui-url"),
                        metadataStore,
                        zipTraceStore,
                        documentStore,
                        publisher,
                        documentStoreBucketName,
                        "1");
    }

    @Test
    void uploadsZipOfAllDocsAndSavesMetadataForGivenNhsNumber()
            throws IllFormedPatientDetailsException, JsonProcessingException {
        var nhsNumber = new NhsNumber("9000000009");
        var location = "s3://" + documentStoreBucketName + "/test";
        var metadataBuilder = DocumentMetadataBuilder.theMetadata().withDocumentUploaded(true);
        var metadataList =
                List.of(
                        metadataBuilder
                                .withFileName("Some document")
                                .withLocation(location)
                                .build(),
                        metadataBuilder
                                .withFileName("another document")
                                .withLocation(location)
                                .build());

        metadataList.forEach(
                metadata -> {
                    metadataStore.save(metadata);
                    var input = IOUtils.toInputStream("Test data", "UTF-8");
                    documentStore.addDocument(new DocumentLocation(location), input);
                });

        var requestEvent = createRequestEvent(nhsNumber);
        var responseEvent =
                createDocumentManifestByNhsNumberHandler.handleRequest(requestEvent, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        var responseUrl = JsonPath.<String>read(responseEvent.getBody(), "$.result.url");
        assertThat(responseUrl).contains("patient-record-" + nhsNumber.getValue());

        verify(publisher).publish(any(DownloadAllPatientRecordsAuditMessage.class));
    }

    private APIGatewayProxyRequestEvent createRequestEvent(NhsNumber nhsNumber) {
        HashMap<String, String> headers = new HashMap<>();
        HashMap<String, String> parameters = new HashMap<>();
        headers.put("Authorization", "Bearer " + JWT.create().withClaim("email", "").sign(none()));
        parameters.put(
                "subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber.getValue());

        return new APIGatewayProxyRequestEvent()
                .withQueryStringParameters(parameters)
                .withHeaders(headers);
    }
}
