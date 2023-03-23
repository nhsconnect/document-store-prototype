package uk.nhs.digital.docstore.services;

import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.model.DocumentLocation;

public class VirusScannedEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirusScannedEventService.class);

    private final DocumentMetadataStore metadataStore;

    private final Clock clock;

    public VirusScannedEventService(
            DocumentMetadataStore metadataStore,
            Clock clock) {
        this.metadataStore = metadataStore;
        this.clock = clock;
    }

    public void updateVirusScanResult(DocumentLocation location, String scanResult) {
        var metadata = metadataStore.getByLocation(location);
        if (metadata != null) {
            metadata.setDocumentUploaded(true);
            metadata.setIndexed(Instant.now(clock).toString());
            metadata.setVirusScanResult(scanResult);

            LOGGER.info(
                    "Updating DocumentReference {} to uploaded and adding virusScan result",
                    metadata.getId());
            metadataStore.save(metadata);
        }
    }
}
