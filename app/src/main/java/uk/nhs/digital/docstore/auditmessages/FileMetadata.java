package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

public class FileMetadata {
    private final String id;
    private final String fileName;
    private final String fileType;

    public FileMetadata(String id, String fileName, String fileType) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
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

    public static FileMetadata fromDocumentMetadata(DocumentMetadata documentMetadata){
        return new FileMetadata(documentMetadata.getId(), documentMetadata.getDescription(), documentMetadata.getContentType());
    }
}
