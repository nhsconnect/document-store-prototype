package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.audit.message.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;

public class DocumentReferenceService {
    private final DocumentMetadataStore metadataStore;
    private final AuditPublisher sensitiveIndex;
    private final DocumentMetadataSerialiser serialiser;

    public DocumentReferenceService(
            DocumentMetadataStore documentMetadataStore,
            AuditPublisher sensitiveIndex,
            DocumentMetadataSerialiser serialiser) {
        this.metadataStore = documentMetadataStore;
        this.sensitiveIndex = sensitiveIndex;
        this.serialiser = serialiser;
    }

    public Document save(Document document)
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var documentMetadata = metadataStore.save(serialiser.fromDocumentModel(document));
        sensitiveIndex.publish(new CreateDocumentMetadataAuditMessage(document));

        return serialiser.toDocumentModel(documentMetadata);
    }
}
