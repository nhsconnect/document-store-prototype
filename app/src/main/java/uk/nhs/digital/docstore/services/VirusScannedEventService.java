package uk.nhs.digital.docstore.services;

import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.ScanResult;

public class VirusScannedEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirusScannedEventService.class);

    private final DocumentMetadataStore metadataStore;

    private final Clock clock;

    private final String quarantineBucketName;

    public VirusScannedEventService(DocumentMetadataStore metadataStore) {
        this(metadataStore, Clock.systemUTC(), System.getenv("QUARANTINE_BUCKET_NAME"));
    }

    public VirusScannedEventService(
            DocumentMetadataStore metadataStore, Clock clock, String quarantineBucketName) {
        this.metadataStore = metadataStore;
        this.clock = clock;
        this.quarantineBucketName = quarantineBucketName;
    }

    public void updateVirusScanResult(DocumentLocation location, String scanResult) {
        var metadata = metadataStore.getByLocation(location);
        if (metadata != null) {
            metadata.setDocumentUploaded(true);
            metadata.setIndexed(Instant.now(clock).toString());
            metadata.setVirusScanResult(scanResult);
            if (scanResult.equals(ScanResult.INFECTED.toString())) {
                metadata.setLocation(
                        String.format(
                                "s3://%s/%s/%s",
                                quarantineBucketName,
                                location.getBucketName(),
                                location.getPath()));
            }
            LOGGER.info(
                    "Updating DocumentReference {} to uploaded and adding virusScan result",
                    metadata.getId());
            metadataStore.save(metadata);
        }
    }
}
