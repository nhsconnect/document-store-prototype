package uk.nhs.digital.docstore.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.NhsNumber;

class FileMetadataTest {
  @Test
  public void shouldCreateFileMetadataFromDocumentMetadata()
      throws IllFormedPatientDetailsException {
    var documentId = "2";
    var documentTitle = "Document Title";
    var contentType = "pdf";

    var document =
        new Document(
            documentId,
            new NhsNumber("0123456789"),
            contentType,
            null,
            new FileName(documentTitle),
            null,
            null,
            null,
            null,
            null);

    var fileMetadata = FileMetadata.fromDocument(document);

    assertThat(fileMetadata.getFileName()).isEqualTo(documentTitle);
    assertThat(fileMetadata.getId()).isEqualTo(documentId);
    assertThat(fileMetadata.getFileType()).isEqualTo(contentType);
  }
}
