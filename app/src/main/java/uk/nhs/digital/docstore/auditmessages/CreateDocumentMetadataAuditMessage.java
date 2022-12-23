package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

public class CreateDocumentMetadataAuditMessage extends BaseAuditMessage {
    private final String nhsNumber;
    private final FileMetadata fileMetadata;

    public CreateDocumentMetadataAuditMessage(DocumentMetadata documentMetadata) {
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
