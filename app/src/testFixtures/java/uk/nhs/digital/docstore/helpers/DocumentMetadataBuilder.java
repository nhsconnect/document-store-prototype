package uk.nhs.digital.docstore.helpers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

@SuppressWarnings("unused")
public class DocumentMetadataBuilder {
    private final String id;
    private final NhsNumber nhsNumber;
    private final String location;
    private final String contentType;
    private final Boolean uploaded;
    private final String created;
    private final String indexed;
    private final String deleted;
    private final String fileName;
    private final String type = "SNOMED";
    private final String virusScanResult;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentMetadataBuilder.class);

    public static DocumentMetadataBuilder theMetadata() throws IllFormedPatientDetailsException {
        var nhsNumber = RandomStringUtils.randomNumeric(10);
        var location =
                String.format(
                        "s3://%s/%s",
                        RandomStringUtils.randomAlphabetic(6),
                        RandomStringUtils.randomAlphabetic(10));
        var created = Instant.now().minus(10, ChronoUnit.DAYS);

        return new DocumentMetadataBuilder(
                null,
                new NhsNumber(nhsNumber),
                location,
                "text/plain",
                false,
                created.toString(),
                null,
                null,
                "Document Title",
                "NOT_SCANNED");
    }

    private DocumentMetadataBuilder(
            String id,
            NhsNumber nhsNumber,
            String location,
            String contentType,
            Boolean uploaded,
            String created,
            String indexed,
            String deleted,
            String fileName,
            String virusScanResult) {
        this.id = id;
        this.nhsNumber = nhsNumber;
        this.location = location;
        this.contentType = contentType;
        this.uploaded = uploaded;
        this.created = created;
        this.indexed = indexed;
        this.deleted = deleted;
        this.fileName = fileName;
        this.virusScanResult = virusScanResult;
    }

    public DocumentMetadataBuilder withNhsNumber(NhsNumber nhsNumber) {
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexed,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadataBuilder withLocation(String location) {
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexed,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadataBuilder withContentType(String contentType) {
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexed,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadataBuilder withDocumentUploaded(Boolean uploaded) {
        // TODO: INVESTIGATE WHERE THIS CONSTRUCTORS ARE USED, DO WE NEED THEM TO INSTANTIATE THE
        // VALUES WE NEED WHEN CREATE DOC HAPPENS
        LOGGER.debug("////ISUPLOADED////", uploaded);
        var indexedAt = uploaded ? Instant.now().toString() : null;
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexedAt,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadataBuilder withCreated(String created) {
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexed,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadataBuilder withIndexed(String indexed) {
        // TODO: INVESTIGATE WHERE THIS CONSTRUCTORS ARE USED, DO WE NEED THEM TO INSTANTIATE THE
        // VALUES WE NEED WHEN CREATE DOC HAPPENS
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexed,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadataBuilder withDeleted(String deleted) {
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexed,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadataBuilder withFileName(String fileName) {
        return new DocumentMetadataBuilder(
                id,
                nhsNumber,
                location,
                contentType,
                uploaded,
                created,
                indexed,
                deleted,
                fileName,
                virusScanResult);
    }

    public DocumentMetadata build() {
        var metadata = new DocumentMetadata();
        metadata.setId(RandomStringUtils.randomAlphabetic(10));
        metadata.setNhsNumber(nhsNumber.getValue());
        metadata.setLocation(location);
        metadata.setContentType(contentType);
        metadata.setDocumentUploaded(uploaded);
        metadata.setCreated(created);
        metadata.setIndexed(indexed);
        metadata.setDeleted(deleted);
        metadata.setFileName(fileName);
        metadata.setType(List.of(type));
        metadata.setVirusScanResult(virusScanResult);
        return metadata;
    }
}
