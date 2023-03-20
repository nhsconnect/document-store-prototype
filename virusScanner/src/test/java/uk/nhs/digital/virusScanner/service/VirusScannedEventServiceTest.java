package uk.nhs.digital.virusScanner.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.nhs.digital.virusScanner.data.repository.DocumentMetadataStore;
import uk.nhs.digital.virusScanner.data.serialiser.DocumentMetadataSerialiser;

class VirusScannedEventServiceTest {

    @Mock
    DocumentMetadataStore metadataStore;

    @Mock
    DocumentMetadataSerialiser metadataSerialiser;

    @Test
    public void testSavesVirusScanResultsInDynamoDb() {

    }
}