package uk.nhs.digital.docstore.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

public class ReRegistrationEventHandler implements RequestHandler<SNSEvent, Void> {
    private final DocumentDeletionService deletionService;

    public ReRegistrationEventHandler() {
        this(
                new DocumentDeletionService(
                        new SplunkPublisher(),
                        new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME")),
                        new DocumentMetadataStore(),
                        new DocumentMetadataSerialiser()));
    }

    public ReRegistrationEventHandler(DocumentDeletionService deletionService) {
        this.deletionService = deletionService;
    }

    @Override
    public Void handleRequest(SNSEvent snsEvent, Context context) {
        try {
            var nhsNumber = new NhsNumber("9890123456");

            deletionService.deleteAllDocumentsForPatient(nhsNumber);

        } catch (IllFormedPatientDetailsException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
