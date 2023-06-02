package uk.nhs.digital.docstore.helpers;

import java.time.Instant;
import java.util.List;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.*;

public class DocumentBuilder {
    private final String referenceId;
    private final NhsNumber nhsNumber;
    private final String contentType;
    private final Boolean uploaded;
    private final FileName fileName;
    private final Instant created;
    private final Instant deleted;
    private final Instant indexed;
    private final List<String> type;
    private final DocumentLocation location;
    private final ScanResult virusScanResult;

    public static DocumentBuilder baseDocumentBuilder() {
        try {
            return new DocumentBuilder(
                    "123",
                    new NhsNumber("1234567890"),
                    "pdf",
                    true,
                    new FileName("some title"),
                    Instant.parse("2023-01-21T15:44:42.370623Z"),
                    null,
                    Instant.parse("2023-01-21T15:44:42.372042Z"),
                    List.of("snomed code"),
                    new DocumentLocation("s3://test-bucket/test-path"),
                    ScanResult.NOT_SCANNED);
        } catch (IllFormedPatientDetailsException e) {
            throw new RuntimeException(e);
        }
    }

    public DocumentBuilder(
            String referenceId,
            NhsNumber nhsNumber,
            String contentType,
            Boolean uploaded,
            FileName fileName,
            Instant created,
            Instant deleted,
            Instant indexed,
            List<String> type,
            DocumentLocation location,
            ScanResult virusScanResult) {
        this.referenceId = referenceId;
        this.nhsNumber = nhsNumber;
        this.contentType = contentType;
        this.uploaded = uploaded;
        this.fileName = fileName;
        this.created = created;
        this.deleted = deleted;
        this.indexed = indexed;
        this.type = type;
        this.location = location;
        this.virusScanResult = virusScanResult;
    }

    public DocumentBuilder withNhsNumber(NhsNumber nhsNumber) {
        return new DocumentBuilder(
                referenceId,
                nhsNumber,
                contentType,
                uploaded,
                fileName,
                created,
                deleted,
                indexed,
                type,
                location,
                virusScanResult);
    }

    public DocumentBuilder withFileName(FileName fileName) {
        return new DocumentBuilder(
                referenceId,
                nhsNumber,
                contentType,
                uploaded,
                fileName,
                created,
                deleted,
                indexed,
                type,
                location,
                virusScanResult);
    }

    public DocumentBuilder withVirusScanResult(ScanResult virusScanResult) {
        return new DocumentBuilder(
                referenceId,
                nhsNumber,
                contentType,
                uploaded,
                fileName,
                created,
                deleted,
                indexed,
                type,
                location,
                virusScanResult);
    }

    public Document build() {
        return new Document(
                referenceId,
                nhsNumber,
                contentType,
                uploaded,
                fileName,
                created,
                deleted,
                indexed,
                type,
                location,
                virusScanResult);
    }
}
