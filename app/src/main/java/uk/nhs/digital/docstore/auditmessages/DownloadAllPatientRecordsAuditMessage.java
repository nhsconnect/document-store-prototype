package uk.nhs.digital.docstore.auditmessages;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DownloadAllPatientRecordsAuditMessage extends BaseAuditMessage {
    private final String nhsNumber;
    private final List<FileMetadata> fileMetadataList;

    public DownloadAllPatientRecordsAuditMessage(String nhsNumber, List<DocumentMetadata> metadataList) {
        this.nhsNumber = nhsNumber;
        this.fileMetadataList = metadataList.stream().map(FileMetadata::fromDocumentMetadata).collect(toList());
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public List<FileMetadata> getFileMetadataList() {
        return fileMetadataList;
    }
}
