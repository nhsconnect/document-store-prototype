package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;

public class DocumentDeletionService {
    private final AuditPublisher sensitiveIndexPublisher;

    public DocumentDeletionService(AuditPublisher sensitiveIndexPublisher) {
        this.sensitiveIndexPublisher = sensitiveIndexPublisher;
    }

    public void audit() throws JsonProcessingException {
        sensitiveIndexPublisher.publish(new DeletedAllDocumentsAuditMessage());
    }
}
