package uk.nhs.digital.docstore.model;

import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

public class Document {
    private final String referenceId;
    private final NhsNumber nhsNumber;
    private final String contentType;
    private final Boolean uploaded;
    private final FileName fileName;
    private final Instant created;
    private final Instant deleted;
    private final Instant indexed;
    private final List<String> type;
    private DocumentLocation location;
    private ScanResult scanResult;

    public Document(
            String referenceId,
            NhsNumber nhsNumber,
            String contentType,
            Boolean uploaded,
            FileName fileName,
            Instant created,
            Instant deleted,
            Instant indexed,
            List<String> type,
            DocumentLocation location,
            ScanResult scanResult) {
        this.referenceId = referenceId;
        this.nhsNumber = nhsNumber;
        this.contentType = contentType;
        this.uploaded = uploaded;
        this.fileName = fileName;
        this.created = created;
        this.deleted = deleted;
        this.indexed = indexed;
        this.type = type;
        this.location = location;
        this.scanResult = scanResult;
    }

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

    public FileName getFileName() {
        return fileName;
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

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    @Override
    public String toString() {
        return "Document{"
                + "referenceId='"
                + referenceId
                + '\''
                + ", nhsNumber='"
                + nhsNumber
                + '\''
                + ", contentType='"
                + contentType
                + '\''
                + ", uploaded="
                + uploaded
                + ", fileName='"
                + fileName
                + '\''
                + ", created='"
                + created
                + '\''
                + ", deleted='"
                + deleted
                + '\''
                + ", indexed='"
                + indexed
                + '\''
                + ", type="
                + type
                + '\''
                + ", location='"
                + location
                + '}';
    }
}
