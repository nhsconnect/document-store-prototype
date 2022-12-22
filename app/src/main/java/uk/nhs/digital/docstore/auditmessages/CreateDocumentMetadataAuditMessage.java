package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

public class CreateDocumentMetadataAuditMessage extends BaseAuditMessage {
    private final String id;
    private final String nhsNumber;
    private final String fileName;
    private final String fileType;

    public CreateDocumentMetadataAuditMessage(String id, String nhsNumber, String fileName, String fileType) {
        this.id = id;
        this.nhsNumber = nhsNumber;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public CreateDocumentMetadataAuditMessage(DocumentMetadata documentMetadata) {
        this(
                documentMetadata.getId(),
                documentMetadata.getNhsNumber(),
                documentMetadata.getDescription(),
                documentMetadata.getContentType()
        );
    }

    public String getId() {
        return id;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }
}
