package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.message.ReRegistrationAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.events.ReRegistrationEvent;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

public class DocumentDeletionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDeletionService.class);
    private final AuditPublisher sensitiveIndexPublisher;
    private final DocumentStore documentStore;
    private final DocumentMetadataStore metadataStore;
    private final DocumentMetadataSerialiser serialiser;

    public DocumentDeletionService(
            AuditPublisher sensitiveIndexPublisher,
            DocumentStore documentStore,
            DocumentMetadataStore metadataStore,
            DocumentMetadataSerialiser serialiser) {
        this.sensitiveIndexPublisher = sensitiveIndexPublisher;
        this.documentStore = documentStore;
        this.metadataStore = metadataStore;
        this.serialiser = serialiser;
    }

    public List<Document> deleteAllDocumentsForPatient(NhsNumber nhsNumber) {
        var documentMetadataList = metadataStore.findByNhsNumber(nhsNumber);
        var documentList = new ArrayList<Document>();

        if (documentMetadataList.size() == 0) {
            LOGGER.info("There is no document available for this NHS number.");
        } else {
            LOGGER.debug("Deleting document metadata from DynamoDB");
            documentMetadataList = metadataStore.deleteAndSave(documentMetadataList);

            LOGGER.debug("Deleting documents from S3");
            // TODO: consider using S3 delete objects operation to save network requests, if
            // possible
            documentMetadataList.forEach(
                    documentMetadata -> {
                        try {
                            var document = serialiser.toDocumentModel(documentMetadata);
                            LOGGER.debug(
                                    "Deleting object key: "
                                            + document.getLocation().getPath()
                                            + " from bucket: "
                                            + document.getLocation().getBucketName());
                            documentStore.deleteObjectAtLocation(document.getLocation());
                            documentList.add(document);

                        } catch (IllFormedPatientDetailsException e) {
                            LOGGER.error(e.getMessage());
                        }
                    });
            LOGGER.info("Successfully deleted all documents.");
        }

        return documentList;
    }

    public void deleteAllDocumentsAudit(NhsNumber nhsNumber, List<Document> documentList)
            throws JsonProcessingException {
        sensitiveIndexPublisher.publish(
                new DeletedAllDocumentsAuditMessage(nhsNumber, documentList));
    }

    public void reRegistrationAudit(
            ReRegistrationEvent reRegistrationEvent, List<Document> documentList)
            throws JsonProcessingException {
        sensitiveIndexPublisher.publish(
                new ReRegistrationAuditMessage(reRegistrationEvent, documentList));
    }
}
