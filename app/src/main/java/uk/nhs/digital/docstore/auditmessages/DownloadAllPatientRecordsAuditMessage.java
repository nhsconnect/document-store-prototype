package uk.nhs.digital.docstore.auditmessages;

import java.util.List;

public class DownloadAllPatientRecordsAuditMessage extends BaseAuditMessage {
    private final String nhsNumber;
    private final List<FileMetadata> fileMetadataList;

    public DownloadAllPatientRecordsAuditMessage(String nhsNumber, List<FileMetadata> fileMetadataList) {
        this.nhsNumber = nhsNumber;
        this.fileMetadataList = fileMetadataList;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public List<FileMetadata> getFileMetadataList() {
        return fileMetadataList;
    }
}
