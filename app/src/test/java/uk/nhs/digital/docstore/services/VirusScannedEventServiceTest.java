package uk.nhs.digital.docstore.services;

import static org.mockito.Mockito.*;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;

class VirusScannedEventServiceTest {

    @Test
    public void testSavesVirusScanResultsInDynamoDb() throws IllFormedPatientDetailsException {
        DocumentMetadataStore metadataStore = mock(DocumentMetadataStore.class);
        DocumentMetadataSerialiser metadataSerialiser = mock(DocumentMetadataSerialiser.class);
        var virusScannedEventService =
                new VirusScannedEventService(metadataStore, metadataSerialiser, Clock.systemUTC());
        DocumentLocation location = new DocumentLocation("s3://test/test");
        String scanResult = "Infected";
        var metadata = DocumentMetadataBuilder.theMetadata().build();

        when(metadataStore.getByLocation(location)).thenReturn(metadata);
        virusScannedEventService.updateVirusScanResult(location, scanResult);

        verify(metadataStore).save(metadata);
    }
}
