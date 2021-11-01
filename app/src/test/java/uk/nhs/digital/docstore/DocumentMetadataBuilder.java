package uk.nhs.digital.docstore;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@SuppressWarnings("unused")
public class DocumentMetadataBuilder {
    private final String id;
    private final String nhsNumber;
    private final String location;
    private final String contentType;
    private final Boolean uploaded;

    public static DocumentMetadataBuilder theMetadata() {
        String id = randomAlphabetic(10);
        String nhsNumber = randomNumeric(11);
        String location = String.format(
                "s3://%s/%s",
                randomAlphabetic(6),
                randomAlphabetic(10));
        return new DocumentMetadataBuilder(id, nhsNumber, location, "text/plain", null);
    }

    private DocumentMetadataBuilder(String id, String nhsNumber, String location, String contentType, Boolean uploaded) {
        this.id = id;
        this.nhsNumber = nhsNumber;
        this.location = location;
        this.contentType = contentType;
        this.uploaded = uploaded;
    }

    public DocumentMetadataBuilder withId(String id) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded);
    }

    public DocumentMetadataBuilder withNhsNumber(String nhsNumber) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded);
    }

    public DocumentMetadataBuilder withLocation(String location) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded);
    }

    public DocumentMetadataBuilder withContentType(String contentType) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded);
    }

    public DocumentMetadataBuilder withDocumentUploaded(Boolean uploaded) {
        return new DocumentMetadataBuilder(id, nhsNumber, location, contentType, uploaded);
    }

    public DocumentMetadata build() {
        var metadata = new DocumentMetadata();
        metadata.setId(id);
        metadata.setNhsNumber(nhsNumber);
        metadata.setLocation(location);
        metadata.setContentType(contentType);
        metadata.setDocumentUploaded(uploaded);
        return metadata;
    }
}
