package uk.nhs.digital.docstore.helpers;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

@SuppressWarnings("unused")
public class DocumentMetadataBuilder {
  private final String id;
  private final NhsNumber nhsNumber;
  private final String location;
  private final String contentType;
  private final Boolean uploaded;
  private final String deleted;
  private final String description;

  public static DocumentMetadataBuilder theMetadata() throws IllFormedPatientDetailsException {
    var id = randomAlphabetic(10);
    var nhsNumber = randomNumeric(10);
    var location = String.format("s3://%s/%s", randomAlphabetic(6), randomAlphabetic(10));

    return new DocumentMetadataBuilder(
        id, new NhsNumber(nhsNumber), location, "text/plain", null, null, "Document Title");
  }

  private DocumentMetadataBuilder(
      String id,
      NhsNumber nhsNumber,
      String location,
      String contentType,
      Boolean uploaded,
      String deleted,
      String description) {
    this.id = id;
    this.nhsNumber = nhsNumber;
    this.location = location;
    this.contentType = contentType;
    this.uploaded = uploaded;
    this.deleted = deleted;
    this.description = description;
  }

  public DocumentMetadataBuilder withId(String id) {
    return new DocumentMetadataBuilder(
        id, nhsNumber, location, contentType, uploaded, deleted, description);
  }

  public DocumentMetadataBuilder withNhsNumber(NhsNumber nhsNumber) {
    return new DocumentMetadataBuilder(
        id, nhsNumber, location, contentType, uploaded, deleted, description);
  }

  public DocumentMetadataBuilder withLocation(String location) {
    return new DocumentMetadataBuilder(
        id, nhsNumber, location, contentType, uploaded, deleted, description);
  }

  public DocumentMetadataBuilder withContentType(String contentType) {
    return new DocumentMetadataBuilder(
        id, nhsNumber, location, contentType, uploaded, deleted, description);
  }

  public DocumentMetadataBuilder withDocumentUploaded(Boolean uploaded) {
    return new DocumentMetadataBuilder(
        id, nhsNumber, location, contentType, uploaded, deleted, description);
  }

  public DocumentMetadataBuilder withDeleted(String deleted) {
    return new DocumentMetadataBuilder(
        id, nhsNumber, location, contentType, uploaded, deleted, description);
  }

  public DocumentMetadataBuilder withDescription(String description) {
    return new DocumentMetadataBuilder(
        id, nhsNumber, location, contentType, uploaded, deleted, description);
  }

  public DocumentMetadata build() {
    var metadata = new DocumentMetadata();
    metadata.setId(id);
    metadata.setNhsNumber(nhsNumber.getValue());
    metadata.setLocation(location);
    metadata.setContentType(contentType);
    metadata.setDocumentUploaded(uploaded);
    metadata.setDeleted(deleted);
    metadata.setDescription(description);
    return metadata;
  }
}
