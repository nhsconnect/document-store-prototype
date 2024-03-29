package uk.nhs.digital.docstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.lambdas.DeleteDocumentReferenceHandler;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

@ExtendWith(MockitoExtension.class)
public class DeleteDocumentReferenceTest extends BaseDocumentStoreTest {
    @Mock private Context context;
    @Mock private AuditPublisher auditPublisher;

    private DeleteDocumentReferenceHandler deleteDocumentReferenceHandler;

    private DocumentStore documentStore;

    private DocumentMetadataStore documentMetadataStore;

    @BeforeEach
    void setUp() {
        var apiConfig = new StubbedApiConfig("http://ui-url");

        documentMetadataStore = new DocumentMetadataStore(dynamoDBHelper.getMapper());
        documentStore = new DocumentStore(aws.getS3Client());
        var deletionService =
                new DocumentDeletionService(
                        auditPublisher,
                        documentStore,
                        documentMetadataStore,
                        new DocumentMetadataSerialiser());

        deleteDocumentReferenceHandler =
                new DeleteDocumentReferenceHandler(apiConfig, deletionService);
    }

    @Test
    void deletesDocumentAndPublishesAuditMessage()
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var nhsNumber = new NhsNumber("1234567890");
        var location = "s3://" + documentStoreBucketName + "/test";
        var metadata =
                DocumentMetadataBuilder.theMetadata()
                        .withNhsNumber(nhsNumber)
                        .withDocumentUploaded(true)
                        .withLocation(location)
                        .build();
        var content = "content of file stored in S3";

        documentMetadataStore.save(metadata);
        documentStore.addDocument(
                new DocumentLocation(location), new ByteArrayInputStream(content.getBytes()));

        deleteDocumentReferenceHandler.handleRequest(createRequestEvent(nhsNumber), context);

        assertThat(documentMetadataStore.findByNhsNumber(nhsNumber)).isEmpty();
        assertThrows(
                AmazonS3Exception.class,
                () -> documentStore.getObjectFromS3(new DocumentLocation(metadata.getLocation())));

        verify(auditPublisher).publish(any(DeletedAllDocumentsAuditMessage.class));
    }

    private APIGatewayProxyRequestEvent createRequestEvent(NhsNumber nhsNumber) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(
                "subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber.getValue());

        return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
    }
}
