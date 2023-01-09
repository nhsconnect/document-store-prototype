package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DownloadAllPatientRecordsAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String nhsNumber;
    private final List<FileMetadata> fileMetadataList;

    public DownloadAllPatientRecordsAuditMessage(String nhsNumber, List<DocumentMetadata> metadataList) {
        this.nhsNumber = nhsNumber;
        this.fileMetadataList = metadataList.stream().map(FileMetadata::fromDocumentMetadata).collect(toList());
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
        return "Downloaded documents for patient";
    }
}
