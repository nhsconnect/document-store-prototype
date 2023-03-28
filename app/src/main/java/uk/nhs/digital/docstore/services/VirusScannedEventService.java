package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.audit.message.VirusScannedAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.ScanResult;

public class VirusScannedEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirusScannedEventService.class);

    private final DocumentMetadataStore metadataStore;

    private final Clock clock;

    private final String quarantineBucketName;

    private final AuditPublisher sensitiveIndex;

    private final DocumentMetadataSerialiser metadataSerialiser;

    public VirusScannedEventService(
            DocumentMetadataStore metadataStore, AuditPublisher sensitiveIndex) {
        this(
                metadataStore,
                Clock.systemUTC(),
                System.getenv("QUARANTINE_BUCKET_NAME"),
                sensitiveIndex,
                new DocumentMetadataSerialiser());
    }

    public VirusScannedEventService(
            DocumentMetadataStore metadataStore,
            Clock clock,
            String quarantineBucketName,
            AuditPublisher sensitiveIndex,
            DocumentMetadataSerialiser metadataSerialiser) {
        this.metadataStore = metadataStore;
        this.clock = clock;
        this.quarantineBucketName = quarantineBucketName;
        this.sensitiveIndex = sensitiveIndex;
        this.metadataSerialiser = metadataSerialiser;
    }

    public void updateVirusScanResult(DocumentLocation location, String scanResult)
            throws IllFormedPatientDetailsException, JsonProcessingException {
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
            var document = metadataSerialiser.toDocumentModel(metadata);
            sensitiveIndex.publish(
                    new VirusScannedAuditMessage(
                            document, ScanResult.scanResultFromString(scanResult)));
        } else {
            LOGGER.info("There is no metadata for the location provided");
        }
    }
}
