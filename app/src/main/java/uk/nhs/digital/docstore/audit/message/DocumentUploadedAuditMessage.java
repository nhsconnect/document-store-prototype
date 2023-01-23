package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

public class DocumentUploadedAuditMessage extends PatientRelatedAuditMessage implements AuditMessage {
    private final FileMetadata fileMetadata;

    public DocumentUploadedAuditMessage(DocumentMetadata documentMetadata) throws IllFormedPatentDetailsException {
        super(new NhsNumber(documentMetadata.getNhsNumber()));
        this.fileMetadata = FileMetadata.fromDocumentMetadata(documentMetadata);
    }

    @SuppressWarnings("unused")
    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    @Override
    public String getDescription() {
        return "Uploaded document";
    }
}
