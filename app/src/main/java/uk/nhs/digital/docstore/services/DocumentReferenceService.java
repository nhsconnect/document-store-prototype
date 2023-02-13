package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.audit.message.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.audit.message.DocumentUploadedAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.DocumentLocation;

public class DocumentReferenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentReferenceService.class);

    private final DocumentMetadataStore metadataStore;
    private final AuditPublisher sensitiveIndex;
    private final Clock clock;
    private final DocumentMetadataSerialiser serialiser;

    public DocumentReferenceService(
            DocumentMetadataStore store,
            AuditPublisher sensitiveIndex,
            DocumentMetadataSerialiser serialiser) {
        this(store, sensitiveIndex, serialiser, Clock.systemUTC());
    }

    public DocumentReferenceService(
            DocumentMetadataStore documentMetadataStore,
            AuditPublisher sensitiveIndex,
            DocumentMetadataSerialiser serialiser,
            Clock clock) {
        this.metadataStore = documentMetadataStore;
        this.sensitiveIndex = sensitiveIndex;
        this.serialiser = serialiser;
        this.clock = clock;
    }

    public Document save(Document document)
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var documentMetadata = metadataStore.save(serialiser.fromDocumentModel(document));
        sensitiveIndex.publish(new CreateDocumentMetadataAuditMessage(document));

        return serialiser.toDocumentModel(documentMetadata);
    }

    public void markDocumentUploaded(DocumentLocation location)
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var metadata = metadataStore.getByLocation(location);
        if (metadata != null) {
            metadata.setDocumentUploaded(true);
            metadata.setIndexed(Instant.now(clock).toString());

            LOGGER.debug("Updating DocumentReference {} to uploaded", metadata.getId());
            metadataStore.save(metadata);
            sensitiveIndex.publish(
                    new DocumentUploadedAuditMessage(serialiser.toDocumentModel(metadata)));
        }
    }
}
