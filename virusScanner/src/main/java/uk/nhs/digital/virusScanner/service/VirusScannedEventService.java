package uk.nhs.digital.virusScanner.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.virusScanner.data.repository.DocumentMetadataStore;
import uk.nhs.digital.virusScanner.data.serialiser.DocumentMetadataSerialiser;

public class VirusScannedEventService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(VirusScannedEventService.class);

    private final DocumentMetadataStore metadataStore;

    private final DocumentMetadataSerialiser serialiser;

    public VirusScannedEventService(DocumentMetadataStore metadataStore, DocumentMetadataSerialiser serialiser) {
        this.metadataStore = metadataStore;
        this.serialiser = serialiser;
    }
}
