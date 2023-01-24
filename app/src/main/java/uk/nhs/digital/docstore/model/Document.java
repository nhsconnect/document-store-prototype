package uk.nhs.digital.docstore.model;

import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

import java.time.Instant;
import java.util.List;

public class Document {
    private final String referenceId;
    private final NhsNumber nhsNumber;
    private final String contentType;
    private final Boolean uploaded;
    private final String description;
    private final Instant created;
    private final Instant deleted;
    private final Instant indexed;
    private final List<String> type;
    private DocumentLocation location;


    public Document(
            String referenceId,
            NhsNumber nhsNumber,
            String contentType,
            Boolean uploaded,
            String description,
            Instant created,
            Instant deleted,
            Instant indexed,
            List<String> type,
            DocumentLocation location
    ) {
        this.referenceId = referenceId;
        this.nhsNumber = nhsNumber;
        this.contentType = contentType;
        this.uploaded = uploaded;
        this.description = description;
        this.created = created;
        this.deleted = deleted;
        this.indexed = indexed;
        this.type = type;
        this.location = location;
    };

    public String getReferenceId() {
        return referenceId;
    }

    public NhsNumber getNhsNumber() throws IllFormedPatientDetailsException {
        return nhsNumber;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getDeleted() {
        return deleted;
    }

    public Instant getIndexed() {
        return indexed;
    }

    public List<String> getType() {
        return type;
    }

    public DocumentLocation getLocation() {
        return location;
    }

    public void setLocation(DocumentLocation s3Location) {
        this.location = s3Location;
    }
}
