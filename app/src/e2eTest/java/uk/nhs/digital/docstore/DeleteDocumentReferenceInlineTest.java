package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteDocumentReferenceInlineTest {
    @Mock
    private AmazonS3 s3Client;
    @Mock
    private Context context;
    @Mock
    private DocumentMetadataStore documentMetadataStore;
    @Mock
    private AuditPublisher auditPublisher;

    private DeleteDocumentReferenceHandler deleteDocumentReferenceHandler;

    @BeforeEach
    void setUp() {
        var apiConfig = new StubbedApiConfig("http://ui-url");
        var documentDeletionService = new DocumentDeletionService(auditPublisher);

        deleteDocumentReferenceHandler = new DeleteDocumentReferenceHandler(
                apiConfig,
                s3Client,
                documentMetadataStore,
                documentDeletionService
        );
    }

    @Test
    void sendsAuditMessageUponSuccessfulDeletion() throws JsonProcessingException {
        var s3Location = String.format("s3://%s/%s", "document-store", "some-key");
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setLocation(s3Location);
        var documentMetadataList = List.of(documentMetadata);

        when(documentMetadataStore.findByNhsNumber(any())).thenReturn(documentMetadataList);
        deleteDocumentReferenceHandler.handleRequest(createRequestEvent(), context);

        verify(auditPublisher).publish(any(DeletedAllDocumentsAuditMessage.class));
    }

    private APIGatewayProxyRequestEvent createRequestEvent() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000009");

        return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
    }
}
