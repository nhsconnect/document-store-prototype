package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DownloadAllPatientRecordsAuditMessage extends PatientRelatedAuditMessage implements AuditMessage {
    private final List<FileMetadata> fileMetadataList;

    public DownloadAllPatientRecordsAuditMessage(NhsNumber nhsNumber, List<Document> documentList) {
        super(nhsNumber);
        this.fileMetadataList = documentList.stream().map(FileMetadata::fromDocument).collect(toList());
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
