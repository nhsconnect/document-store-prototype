package uk.nhs.digital.docstore;

import java.net.URL;

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

    @Override
    public String toString() {
        return "Document{" +
                "metadata=" + metadata +
                ", preSignedUrl=" + preSignedUrl +
                '}';
    }
}
