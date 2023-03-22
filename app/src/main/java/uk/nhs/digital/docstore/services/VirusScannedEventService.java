package uk.nhs.digital.docstore.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.model.DocumentLocation;

public class VirusScannedEventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirusScannedEventService.class);

    private final DocumentMetadataStore metadataStore;

    private final DocumentMetadataSerialiser metadataSerialiser;

    public VirusScannedEventService(
            DocumentMetadataStore metadataStore, DocumentMetadataSerialiser metadataSerialiser) {
        this.metadataStore = metadataStore;
        this.metadataSerialiser = metadataSerialiser;
    }

    public void updateVirusScanResult(DocumentLocation location, String scanResult) {
        var metadata = metadataStore.getByLocation(location);
    }
}
