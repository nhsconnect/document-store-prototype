package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;
import java.util.stream.Collectors;

public class DeletedAllDocumentsAuditMessage extends PatientRelatedAuditMessage implements AuditMessage {
    private final List<FileMetadata> fileMetadataList;

    public DeletedAllDocumentsAuditMessage(NhsNumber nhsNumber, List<DocumentMetadata> documentMetadataList) {
        super(nhsNumber);
        this.fileMetadataList = documentMetadataList.stream().map(FileMetadata::fromDocumentMetadata).collect(Collectors.toList());
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
