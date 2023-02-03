package uk.nhs.digital.docstore.audit.message;

import java.util.List;
import java.util.stream.Collectors;
import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.events.ReRegistrationEvent;
import uk.nhs.digital.docstore.model.Document;

public class ReRegistrationAuditMessage extends PatientRelatedAuditMessage implements AuditMessage {
    private final List<FileMetadata> fileMetadataList;
    private final String nemsMessageId;

    public ReRegistrationAuditMessage(
            ReRegistrationEvent reRegistrationEvent, List<Document> documentList) {
        super(reRegistrationEvent.getNhsNumber());
        this.fileMetadataList =
                documentList.stream().map(FileMetadata::fromDocument).collect(Collectors.toList());
        this.nemsMessageId = reRegistrationEvent.getNemsMessageId();
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
