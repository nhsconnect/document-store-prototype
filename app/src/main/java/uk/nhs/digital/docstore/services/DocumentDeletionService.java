package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;

public class DocumentDeletionService {
    private final AuditPublisher sensitiveIndexPublisher;

    public DocumentDeletionService(AuditPublisher sensitiveIndexPublisher) {
        this.sensitiveIndexPublisher = sensitiveIndexPublisher;
    }

    public void audit(NhsNumber nhsNumber, List<DocumentMetadata> documentMetadataList) throws JsonProcessingException {
        sensitiveIndexPublisher.publish(new DeletedAllDocumentsAuditMessage(nhsNumber, documentMetadataList));
    }
}
