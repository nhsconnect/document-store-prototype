package uk.nhs.digital.docstore;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;

public class Document {
    private final DocumentMetadata metadata;

    public Document(DocumentMetadata metadata) {
        this.metadata = metadata;
    }

    public String getReferenceId() {
        return metadata.getId();
    }

    public NhsNumber getNhsNumber() throws IllFormedPatentDetailsException {
        return new NhsNumber(metadata.getNhsNumber());
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

    public String getDeleted(){return metadata.getDeleted();}

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
