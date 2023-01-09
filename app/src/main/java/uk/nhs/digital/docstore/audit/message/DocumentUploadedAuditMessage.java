package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

public class DocumentUploadedAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String nhsNumber;
    private final FileMetadata fileMetadata;

    public DocumentUploadedAuditMessage(DocumentMetadata documentMetadata) {
        this.nhsNumber = documentMetadata.getNhsNumber();
        this.fileMetadata = FileMetadata.fromDocumentMetadata(documentMetadata);
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    @SuppressWarnings("unused")
    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    @Override
    public String getDescription() {
        return "Document uploaded";
    }
}
