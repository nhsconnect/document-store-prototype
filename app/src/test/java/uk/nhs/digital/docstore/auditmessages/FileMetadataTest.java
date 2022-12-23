package uk.nhs.digital.docstore.auditmessages;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import static org.assertj.core.api.Assertions.assertThat;


class FileMetadataTest {
    @Test
    public void shouldCreateFileMetadataFromDocumentMetadata() {
        var documentMetadataId = "2";
        var documentTitle = "Document Title";
        var contentType = "pdf";

        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId(documentMetadataId);
        documentMetadata.setDescription(documentTitle);
        documentMetadata.setContentType(contentType);

        var fileMetadata = FileMetadata.fromDocumentMetadata(documentMetadata);

        assertThat(fileMetadata.getFileName()).isEqualTo(documentTitle);
        assertThat(fileMetadata.getId()).isEqualTo(documentMetadataId);
        assertThat(fileMetadata.getFileType()).isEqualTo(contentType);
    }

}