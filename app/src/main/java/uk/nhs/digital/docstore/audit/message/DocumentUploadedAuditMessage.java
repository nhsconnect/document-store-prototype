package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;

public class DocumentUploadedAuditMessage extends PatientRelatedAuditMessage
        implements AuditMessage {
    private final FileMetadata fileMetadata;

    public DocumentUploadedAuditMessage(Document document) throws IllFormedPatientDetailsException {
        super(document.getNhsNumber());
        this.fileMetadata = FileMetadata.fromDocument(document);
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
