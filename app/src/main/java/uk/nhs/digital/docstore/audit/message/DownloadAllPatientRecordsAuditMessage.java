package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DownloadAllPatientRecordsAuditMessage extends PatientRelatedAuditMessage implements AuditMessage {
    private final List<FileMetadata> fileMetadataList;

    public DownloadAllPatientRecordsAuditMessage(NhsNumber nhsNumber, List<DocumentMetadata> metadataList) {
        super(nhsNumber);
        this.fileMetadataList = metadataList.stream().map(FileMetadata::fromDocumentMetadata).collect(toList());
    }

    @SuppressWarnings("unused")
    public List<FileMetadata> getFileMetadataList() {
        return fileMetadataList;
    }

    @Override
    public String getDescription() {
        return "Downloaded documents";
    }
}
