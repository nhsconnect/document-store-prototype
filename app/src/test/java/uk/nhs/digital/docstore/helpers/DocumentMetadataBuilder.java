package uk.nhs.digital.docstore.helpers;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@SuppressWarnings("unused")
public class DocumentMetadataBuilder {
    private final String id;
    private final String nhsNumber;
    private final String location;
    private final String contentType;
    private final Boolean uploaded;
    private final String deleted;

    public static DocumentMetadataBuilder theMetadata() {
        var id = randomAlphabetic(10);
        var nhsNumber = randomNumeric(11);
        var location = String.format("s3://%s/%s", randomAlphabetic(6), randomAlphabetic(10));

        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                "text/plain",
                null,
                null
        );
    }

    private DocumentMetadataBuilder(
            String id,
            String nhsNumber,
            String location,
            String contentType,
            Boolean uploaded,
            String deleted
    ) {
        this.id = id;
        this.nhsNumber = nhsNumber;
        this.location = location;
        this.contentType = contentType;
        this.uploaded = uploaded;
        this.deleted = deleted;
    }

    public DocumentMetadataBuilder withId(String id) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded, deleted);
    }

    public DocumentMetadataBuilder withNhsNumber(String nhsNumber) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded, deleted);
    }

    public DocumentMetadataBuilder withLocation(String location) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded, deleted);
    }

    public DocumentMetadataBuilder withContentType(String contentType) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded, deleted);
    }

    public DocumentMetadataBuilder withDocumentUploaded(Boolean uploaded) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded, deleted);
    }

    public DocumentMetadataBuilder withDeleted(String deleted) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded, deleted);
    }

    public DocumentMetadata build() {
        var metadata = new DocumentMetadata();
        metadata.setId(id);
        metadata.setNhsNumber(nhsNumber);
        metadata.setLocation(location);
        metadata.setContentType(contentType);
        metadata.setDocumentUploaded(uploaded);
        metadata.setDeleted(deleted);
        return metadata;
    }
}
