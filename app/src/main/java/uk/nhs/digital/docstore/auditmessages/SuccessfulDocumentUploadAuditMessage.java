package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

public class SuccessfulDocumentUploadAuditMessage extends BaseAuditMessage {
    private final String id;
    private final String fileName;
    private final String fileType;

    public SuccessfulDocumentUploadAuditMessage(String id, String fileName, String fileType) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public SuccessfulDocumentUploadAuditMessage(DocumentMetadata documentMetadata) {
        this(
                documentMetadata.getId(),
                documentMetadata.getDescription(),
                documentMetadata.getContentType()
        );
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }
}
