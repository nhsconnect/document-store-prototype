package uk.nhs.digital.docstore.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.events.SqsMessageEvent;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

public class ReRegistrationEventHandler implements RequestHandler<SQSEvent, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReRegistrationEventHandler.class);
    private final DocumentDeletionService deletionService;

    public ReRegistrationEventHandler() {
        this(
                new DocumentDeletionService(
                        new SplunkPublisher(System.getenv("NEMS_SQS_AUDIT_QUEUE_URL")),
                        new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME")),
                        new DocumentMetadataStore(),
                        new DocumentMetadataSerialiser()));
    }

    public ReRegistrationEventHandler(DocumentDeletionService deletionService) {
        this.deletionService = deletionService;
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        sqsEvent.getRecords()
                .forEach(
                        record -> {
                            LOGGER.info("Received new message from re-registration queue.");
                            var message = record.getBody();
                            try {
                                var sqsMessageEvent = SqsMessageEvent.parse(message);
                                LOGGER.info("Parsing message to ReRegistrationEvent.");
                                var reRegistrationEvent = sqsMessageEvent.getMessage();
                                LOGGER.info("Deleting all documents for patient...");
                                var deletedDocuments =
                                        deletionService.deleteAllDocumentsForPatient(
                                                reRegistrationEvent.getNhsNumber());
                                deletionService.reRegistrationAudit(
                                        reRegistrationEvent, deletedDocuments);
                                LOGGER.info("Successfully deleted all documents.");
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage());
                                throw new RuntimeException(e);
                            }
                        });

        return null;
    }
}
