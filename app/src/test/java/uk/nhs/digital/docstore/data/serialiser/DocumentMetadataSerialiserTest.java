package uk.nhs.digital.docstore.data.serialiser;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.NhsNumber;

class DocumentMetadataSerialiserTest {
  @Test
  void shouldDeserialiseToDocumentModel() throws IllFormedPatientDetailsException {
    var nhsNumber = "1234567890";
    var contentType = "pdf";
    var location = "some-location";
    var description = "doc-name";
    var created = Instant.now();
    var type = "some-type";

    var metadata = new DocumentMetadata();
    metadata.setNhsNumber(nhsNumber);
    metadata.setContentType(contentType);
    metadata.setLocation(location);
    metadata.setDocumentUploaded(false);
    metadata.setDescription(description);
    metadata.setCreated(created.toString());
    metadata.setType(List.of(type));

    var document = new DocumentMetadataSerialiser().toDocumentModel(metadata);

    assertThat(document.getNhsNumber()).isEqualTo(new NhsNumber(nhsNumber));
    assertThat(document.getContentType()).isEqualTo(contentType);
    assertThat(document.getLocation().toString()).isEqualTo(location);
    assertThat(document.isUploaded()).isFalse();
    assertThat(document.getDescription()).isEqualTo(new FileName(description));
    assertThat(document.getCreated()).isEqualTo(created);
    assertThat(document.getType().get(0)).isEqualTo(type);
    assertThat(document.getIndexed()).isNull();
    assertThat(document.getDeleted()).isNull();
  }

  @Test
  void shouldSerialiseFromDocumentModel() throws IllFormedPatientDetailsException {
    var nhsNumber = "1234567890";
    var contentType = "pdf";
    var location = "s3://some-bucket/some-path";
    var description = "doc-name";
    var created = Instant.now();
    var type = "some-type";

    var document =
        new Document(
            null,
            new NhsNumber(nhsNumber),
            contentType,
            false,
            new FileName(description),
            created,
            null,
            null,
            List.of(type),
            new DocumentLocation(location));

    var metadata = new DocumentMetadataSerialiser().fromDocumentModel(document);

    assertThat(metadata.getNhsNumber()).isEqualTo(nhsNumber);
    assertThat(metadata.getContentType()).isEqualTo(contentType);
    assertThat(metadata.getLocation()).isEqualTo(location);
    assertThat(metadata.isDocumentUploaded()).isFalse();
    assertThat(metadata.getDescription()).isEqualTo(description);
    assertThat(metadata.getCreated()).isEqualTo(created.toString());
    assertThat(metadata.getType().get(0)).isEqualTo(type);
    assertThat(metadata.getIndexed()).isNull();
    assertThat(metadata.getDeleted()).isNull();
  }
}
