package uk.nhs.digital.docstore.audit;

import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.FileName;

public class FileMetadata {
    private final String id;
    private final FileName fileName;
    private final String fileType;

    public FileMetadata(String id, FileName fileName, String fileType) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName.getValue();
    }

    public String getFileType() {
        return fileType;
    }

    public static FileMetadata fromDocument(Document document){
        return new FileMetadata(document.getReferenceId(), document.getDescription(), document.getContentType());
    }
}
