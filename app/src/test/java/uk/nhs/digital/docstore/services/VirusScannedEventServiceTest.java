package uk.nhs.digital.docstore.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;

class VirusScannedEventServiceTest {

    @Mock DocumentMetadataStore metadataStore;

    @Mock DocumentMetadataSerialiser metadataSerialiser;

    @Test
    public void testSavesVirusScanResultsInDynamoDb() {}
}
