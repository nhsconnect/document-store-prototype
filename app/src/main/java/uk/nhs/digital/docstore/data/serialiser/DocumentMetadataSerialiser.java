package uk.nhs.digital.docstore.data.serialiser;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.time.Instant;

public class DocumentMetadataSerialiser {

  public DocumentMetadata fromDocumentModel(Document document)
      throws IllFormedPatientDetailsException {
    var documentMetadata = new DocumentMetadata();
    documentMetadata.setId(document.getReferenceId());
    documentMetadata.setNhsNumber(document.getNhsNumber().getValue());
    documentMetadata.setContentType(document.getContentType());
    documentMetadata.setLocation(document.getLocation().toString());
    documentMetadata.setDocumentUploaded(document.isUploaded());
    documentMetadata.setFileName(document.getFileName().getValue());
    documentMetadata.setCreated(document.getCreated().toString());
    documentMetadata.setType(document.getType());
    return documentMetadata;
  }

  public Document toDocumentModel(DocumentMetadata metadata)
      throws IllFormedPatientDetailsException {
    return new Document(
        metadata.getId(),
        new NhsNumber(metadata.getNhsNumber()),
        metadata.getContentType(),
        metadata.isDocumentUploaded(),
        new FileName(metadata.getFileName()),
        metadata.getCreated() == null ? null : Instant.parse(metadata.getCreated()),
        metadata.getDeleted() == null ? null : Instant.parse(metadata.getDeleted()),
        metadata.getIndexed() == null ? null : Instant.parse(metadata.getIndexed()),
        metadata.getType(),
        new DocumentLocation(metadata.getLocation()));
  }
}
