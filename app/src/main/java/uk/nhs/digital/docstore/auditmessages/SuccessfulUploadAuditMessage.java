package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import java.time.Instant;

public class SuccessfulUploadAuditMessage extends BaseAuditMessage {
    private final String id;
    private final String fileName;
    private final String fileType;
    private final Instant uploaded;

    public SuccessfulUploadAuditMessage(String id, String fileName, String fileType, Instant uploaded) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploaded = uploaded;
    }

    public SuccessfulUploadAuditMessage(DocumentMetadata documentMetadata) {
        this(
                documentMetadata.getId(),
                documentMetadata.getDescription(),
                documentMetadata.getContentType(),
                Instant.parse(documentMetadata.getIndexed())
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

    public Instant getUploaded() {
        return uploaded;
    }
}
