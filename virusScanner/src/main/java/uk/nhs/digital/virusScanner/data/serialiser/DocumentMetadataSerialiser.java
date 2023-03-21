package uk.nhs.digital.virusScanner.data.serialiser;

import java.time.Instant;
import uk.nhs.digital.virusScanner.data.entity.DocumentMetadata;
import uk.nhs.digital.virusScanner.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.virusScanner.model.*;

public class DocumentMetadataSerialiser {

    public DocumentMetadata fromDocumentModel(Document document) {
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId(document.getReferenceId());
        documentMetadata.setNhsNumber(document.getNhsNumber().getValue());
        documentMetadata.setContentType(document.getContentType());
        documentMetadata.setLocation(document.getLocation().toString());
        documentMetadata.setDocumentUploaded(document.isUploaded());
        documentMetadata.setFileName(document.getFileName().getValue());
        documentMetadata.setCreated(document.getCreated().toString());
        documentMetadata.setType(document.getType());
        documentMetadata.setVirusScanResult(document.getVirusScanResult().toString());
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
                new DocumentLocation(metadata.getLocation()),
                ScanResult.valueOf(metadata.getVirusScanResult()));
    }
}
