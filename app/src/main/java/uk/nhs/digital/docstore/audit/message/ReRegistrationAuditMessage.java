package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;
import java.util.stream.Collectors;

public class ReRegistrationAuditMessage extends PatientRelatedAuditMessage implements AuditMessage {
    private final List<FileMetadata> fileMetadataList;
    private final String nemsMessageId;

    public ReRegistrationAuditMessage(
            NhsNumber nhsNumber, List<Document> documentList, String nemsMessageId) {
        super(nhsNumber);
        this.fileMetadataList =
                documentList.stream().map(FileMetadata::fromDocument).collect(Collectors.toList());
        this.nemsMessageId = nemsMessageId;
    }

    public String getNemsMessageId() {
        return nemsMessageId;
    }

    public List<FileMetadata> getFileMetadataList() {
        return fileMetadataList;
    }

    @Override
    public String getDescription() {
        return "Deleted documents for re-registered patients";
    }
}
