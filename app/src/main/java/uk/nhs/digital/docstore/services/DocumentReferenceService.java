package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.auditmessages.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.AuditPublisher;

public class DocumentReferenceService {
    private final DocumentMetadataStore store;
    private final AuditPublisher auditPublisher;

    public DocumentReferenceService(DocumentMetadataStore store, AuditPublisher auditPublisher) {
        this.store = store;
        this.auditPublisher = auditPublisher;
    }

    public DocumentMetadata save(DocumentMetadata documentMetadata) throws JsonProcessingException {
        documentMetadata = store.save(documentMetadata);
        auditPublisher.publish(new CreateDocumentMetadataAuditMessage(documentMetadata));
        return documentMetadata;
    }
}
