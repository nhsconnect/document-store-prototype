package uk.nhs.digital.docstore.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.model.DocumentLocation;

class VirusScannedEventServiceTest {

    @Test
    public void testSavesVirusScanResultsInDynamoDb() {
        DocumentMetadataStore metadataStore = mock(DocumentMetadataStore.class);
        DocumentMetadataSerialiser metadataSerialiser = mock(DocumentMetadataSerialiser.class);
        var virusScannedEventService =
                new VirusScannedEventService(metadataStore, metadataSerialiser);
        DocumentLocation location = new DocumentLocation("s3://test/test");
        String scanResult = "Infected";

        virusScannedEventService.updateVirusScanResult(location, scanResult);

        verify(metadataStore).getByLocation(location);
    }
}
