package uk.nhs.digital.docstore.auditmessages;

public class SuccessfulDocumentUploadAuditMessage extends BaseAuditMessage {
    private final FileMetadata fileMetadata;

    public SuccessfulDocumentUploadAuditMessage(FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }
}
