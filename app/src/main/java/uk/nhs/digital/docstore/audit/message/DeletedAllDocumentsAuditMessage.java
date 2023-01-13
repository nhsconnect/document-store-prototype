package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import java.util.List;
import java.util.stream.Collectors;

public class DeletedAllDocumentsAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String nhsNumber;
    private final List<FileMetadata> fileMetadataList;

    public DeletedAllDocumentsAuditMessage(String nhsNumber, List<DocumentMetadata> documentMetadataList) {
        this.nhsNumber = nhsNumber;
        this.fileMetadataList = documentMetadataList.stream().map(FileMetadata::fromDocumentMetadata).collect(Collectors.toList());
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    @SuppressWarnings("unused")
    public List<FileMetadata> getFileMetadataList() {
        return fileMetadataList;
    }

    @Override
    public String getDescription() {
        return "Deleted documents";
    }
}
