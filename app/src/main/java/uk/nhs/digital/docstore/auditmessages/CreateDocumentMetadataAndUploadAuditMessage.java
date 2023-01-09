package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

@SuppressWarnings("unused")
public class CreateDocumentMetadataAndUploadAuditMessage extends BaseAuditMessage {
    private final String nhsNumber;
    private final FileMetadata fileMetadata;

    public CreateDocumentMetadataAndUploadAuditMessage(DocumentMetadata documentMetadata) {
        this.nhsNumber = documentMetadata.getNhsNumber();
        this.fileMetadata = FileMetadata.fromDocumentMetadata(documentMetadata);
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }
}
