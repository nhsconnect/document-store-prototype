package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

@SuppressWarnings("unused")
public class CreateDocumentMetadataAndUploadAuditMessage extends BaseAuditMessage {
    private final String nhsNumber;
    private final FileMetadata fileMetadata;

    private final boolean isDocumentUploadedToS3;

    public CreateDocumentMetadataAndUploadAuditMessage(DocumentMetadata documentMetadata) {
        this.nhsNumber = documentMetadata.getNhsNumber();
        this.fileMetadata = FileMetadata.fromDocumentMetadata(documentMetadata);
        this.isDocumentUploadedToS3 = documentMetadata.isDocumentUploaded();
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public boolean getIsDocumentUploadedToS3() {
        return isDocumentUploadedToS3;
    }
}
