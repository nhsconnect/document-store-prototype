package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.auditmessages.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.auditmessages.SuccessfulUploadAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.AuditPublisher;

import java.time.Instant;

public class DocumentReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentReferenceService.class);
    private final DocumentMetadataStore store;
    private final AuditPublisher auditPublisher;
    private final Instant now;

    public DocumentReferenceService(DocumentMetadataStore store, AuditPublisher auditPublisher) {
        this(store, auditPublisher, Instant.now());
    }

    public DocumentReferenceService(DocumentMetadataStore documentMetadataStore, AuditPublisher auditPublisher, Instant now) {
        this.store = documentMetadataStore;
        this.auditPublisher = auditPublisher;
        this.now = now;
    }

    public DocumentMetadata save(DocumentMetadata documentMetadata) throws JsonProcessingException {
        documentMetadata = store.save(documentMetadata);
        auditPublisher.publish(new CreateDocumentMetadataAuditMessage(documentMetadata));
        return documentMetadata;
    }

    public void markDocumentUploaded(String location) throws JsonProcessingException {
        var metadata = store.getByLocation(location);
        metadata.setDocumentUploaded(true);
        metadata.setIndexed(now.toString());

        LOGGER.debug("Updating DocumentReference {} to uploaded", metadata.getId());
        store.save(metadata);

        auditPublisher.publish(new SuccessfulUploadAuditMessage(metadata));
    }
}
