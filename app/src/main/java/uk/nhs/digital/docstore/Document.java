package uk.nhs.digital.docstore;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import java.util.List;

public class Document {
    private final DocumentMetadata metadata;

    public Document(DocumentMetadata metadata) {
        this.metadata = metadata;
    }

    public String getReferenceId() {
        return metadata.getId();
    }

    public String getNhsNumber() {
        return metadata.getNhsNumber();
    }

    public String getContentType() {
        return metadata.getContentType();
    }

    public boolean isUploaded() {
        return metadata.isDocumentUploaded();
    }

    public String getDescription() {
        return metadata.getDescription();
    }

    public String getCreated() {
        return metadata.getCreated();
    }

    public String getIndexed() {
        return metadata.getIndexed();
    }

    public List<String> getType() {
        return metadata.getType();
    }

    @Override
    public String toString() {
        return "Document{" +
                "metadata=" + metadata +
                '}';
    }
}
