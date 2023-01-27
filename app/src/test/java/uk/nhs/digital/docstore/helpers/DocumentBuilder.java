package uk.nhs.digital.docstore.helpers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.NhsNumber;

public class DocumentBuilder {
  private final String referenceId;
  private final NhsNumber nhsNumber;
  private final String contentType;
  private final Boolean uploaded;
  private final FileName description;
  private final Instant created;
  private final Instant deleted;
  private final Instant indexed;
  private final List<String> type;
  private final DocumentLocation location;

  public DocumentBuilder(
      String referenceId,
      NhsNumber nhsNumber,
      String contentType,
      Boolean uploaded,
      FileName description,
      Instant created,
      Instant deleted,
      Instant indexed,
      List<String> type,
      DocumentLocation location) {
    this.referenceId = referenceId;
    this.nhsNumber = nhsNumber;
    this.contentType = contentType;
    this.uploaded = uploaded;
    this.description = description;
    this.created = created;
    this.deleted = deleted;
    this.indexed = indexed;
    this.type = type;
    this.location = location;
  }

  public static DocumentBuilder baseDocumentBuilder() {
    try {
      return new DocumentBuilder(
          "123",
          new NhsNumber("1234567890"),
          "pdf",
          true,
          new FileName("some title"),
          Instant.now().minus(10, ChronoUnit.DAYS),
          null,
          Instant.now().minus(10, ChronoUnit.DAYS).plus(10, ChronoUnit.SECONDS),
          List.of("snomed code"),
          new DocumentLocation("s3://test-bucket/test-path"));
    } catch (IllFormedPatientDetailsException e) {
      throw new RuntimeException(e);
    }
  }

  public Document build() {
    return new Document(
        referenceId,
        nhsNumber,
        contentType,
        uploaded,
        description,
        created,
        deleted,
        indexed,
        type,
        location);
  }
}
