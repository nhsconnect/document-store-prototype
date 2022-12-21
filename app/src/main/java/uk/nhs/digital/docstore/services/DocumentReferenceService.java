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
    private final AuditPublisher sensitiveIndex;
    private final Instant now;

    public DocumentReferenceService(DocumentMetadataStore store, AuditPublisher sensitiveIndex) {
        this(store, sensitiveIndex, Instant.now());
    }

    public DocumentReferenceService(DocumentMetadataStore documentMetadataStore, AuditPublisher sensitiveIndex, Instant now) {
        this.store = documentMetadataStore;
        this.sensitiveIndex = sensitiveIndex;
        this.now = now;
    }

    public DocumentMetadata save(DocumentMetadata documentMetadata) throws JsonProcessingException {
        documentMetadata = store.save(documentMetadata);
        sensitiveIndex.publish(new CreateDocumentMetadataAuditMessage(documentMetadata));
        return documentMetadata;
    }

    public void markDocumentUploaded(String location) throws JsonProcessingException {
        var metadata = store.getByLocation(location);
        metadata.setDocumentUploaded(true);
        metadata.setIndexed(now.toString());

        LOGGER.debug("Updating DocumentReference {} to uploaded", metadata.getId());
        store.save(metadata);

        sensitiveIndex.publish(new SuccessfulUploadAuditMessage(metadata));
    }
}
