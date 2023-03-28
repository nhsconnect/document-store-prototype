package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.audit.FileMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.ScanResult;

public class VirusScannedAuditMessage extends PatientRelatedAuditMessage implements AuditMessage {
    private final FileMetadata fileMetadata;
    private final ScanResult scanResult;

    public VirusScannedAuditMessage(Document document, ScanResult scanResult)
            throws IllFormedPatientDetailsException {
        super(document.getNhsNumber());
        this.fileMetadata = FileMetadata.fromDocument(document);
        this.scanResult = scanResult;
    }

    @SuppressWarnings("unused")
    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    @Override
    public String getDescription() {
        return "Virus scanned document";
    }
}
