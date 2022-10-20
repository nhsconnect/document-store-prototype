package uk.nhs.digital.docstore.presignedurl;

import uk.nhs.digital.docstore.Document;
import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.DocumentStore.DocumentDescriptor;
import java.net.URL;
import java.util.Map;

class PresignedUrlService {
    private final DocumentMetadataStore metadataStore;
    private final DocumentStore documentStore;

    public PresignedUrlService(DocumentMetadataStore metadataStore, DocumentStore documentStore) {
        this.metadataStore = metadataStore;
        this.documentStore = documentStore;
    }

    public Document findByParameters(Map<String, String> parameters) {
        var metadata = metadataStore.getById(parameters.get("id"));
        return new Document(metadata, getPreSignedUrl(metadata));
    }

    private URL getPreSignedUrl(DocumentMetadata metadata) {
        if (!metadata.isDocumentUploaded()) {
            return null;
        }

        var descriptor = DocumentDescriptor.from(metadata);
        return documentStore.generatePreSignedUrl(descriptor);
    }
}
