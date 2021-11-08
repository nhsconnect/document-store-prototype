package uk.nhs.digital.docstore;

import java.net.URL;
import java.util.List;

public class Document {
    private final DocumentMetadata metadata;
    private final URL preSignedUrl;

    public Document(DocumentMetadata metadata, URL preSignedUrl) {
        this.metadata = metadata;
        this.preSignedUrl = preSignedUrl;
    }

    public String getReferenceId() {
        return metadata.getId();
    }

    public String getNhsNumber() {
        return metadata.getNhsNumber();
    }

    public URL getPreSignedUrl() {
        return preSignedUrl;
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
                ", preSignedUrl=" + preSignedUrl +
                '}';
    }
}
