package uk.nhs.digital.docstore.audit.message;

import java.util.List;
import java.util.stream.Collectors;
import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

public class DeletedAllDocumentsAuditMessage extends PatientRelatedAuditMessage
    implements AuditMessage {
  private final List<FileMetadata> fileMetadataList;

  public DeletedAllDocumentsAuditMessage(NhsNumber nhsNumber, List<Document> documentList) {
    super(nhsNumber);
    this.fileMetadataList =
        documentList.stream().map(FileMetadata::fromDocument).collect(Collectors.toList());
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
