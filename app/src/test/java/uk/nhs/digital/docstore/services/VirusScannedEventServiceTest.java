package uk.nhs.digital.docstore.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.AuditMessage;
import uk.nhs.digital.docstore.audit.message.VirusScannedAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.ScanResult;

@ExtendWith(MockitoExtension.class)
class VirusScannedEventServiceTest {
    @Mock SplunkPublisher sensitiveIndex;
    @Mock DocumentMetadataStore metadataStore;
    @Mock DocumentMetadataSerialiser metadataSerialiser;
    @Captor private ArgumentCaptor<AuditMessage> auditMessageCaptor;

    private static final String QUARANTINE_BUCKET_NAME = "test-bucket";

    @Test
    public void testSavesVirusScanResultsInDynamoDb()
            throws IllFormedPatientDetailsException, JsonProcessingException {
        var virusScannedEventService =
                new VirusScannedEventService(
                        metadataStore,
                        Clock.systemUTC(),
                        QUARANTINE_BUCKET_NAME,
                        sensitiveIndex,
                        metadataSerialiser);
        DocumentLocation location = new DocumentLocation("s3://test/test");
        String scanResult = "Infected";
        var metadata = DocumentMetadataBuilder.theMetadata().build();
        var document = DocumentBuilder.baseDocumentBuilder().build();

        when(metadataStore.getByLocation(location)).thenReturn(metadata);
        when(metadataSerialiser.toDocumentModel(metadata)).thenReturn(document);
        virusScannedEventService.updateVirusScanResult(location, scanResult);

        verify(metadataStore).save(metadata);
    }

    @Test
    public void testDoesNotSaveToDynamoDbIfNoDocumentFoundByLocation()
            throws IllFormedPatientDetailsException, JsonProcessingException {
        var virusScannedEventService =
                new VirusScannedEventService(
                        metadataStore,
                        Clock.systemUTC(),
                        QUARANTINE_BUCKET_NAME,
                        sensitiveIndex,
                        metadataSerialiser);
        DocumentLocation location = new DocumentLocation("s3://test/test");
        String scanResult = "Infected";

        when(metadataStore.getByLocation(location)).thenReturn(null);
        virusScannedEventService.updateVirusScanResult(location, scanResult);

        verify(metadataStore, times(0)).save(any());
    }

    @Test
    public void testSentAuditMessage()
            throws IllFormedPatientDetailsException, JsonProcessingException {
        var virusScannedEventService =
                new VirusScannedEventService(
                        metadataStore,
                        Clock.systemUTC(),
                        QUARANTINE_BUCKET_NAME,
                        sensitiveIndex,
                        metadataSerialiser);
        DocumentLocation location = new DocumentLocation("s3://test/test");
        String scanResult = "Infected";
        var metadata = DocumentMetadataBuilder.theMetadata().build();
        var document = DocumentBuilder.baseDocumentBuilder().build();
        var expectedSensitiveAuditMessage =
                new VirusScannedAuditMessage(document, ScanResult.scanResultFromString(scanResult));

        when(metadataStore.getByLocation(location)).thenReturn(metadata);
        when(metadataSerialiser.toDocumentModel(metadata)).thenReturn(document);
        virusScannedEventService.updateVirusScanResult(location, scanResult);

        verify(sensitiveIndex).publish(auditMessageCaptor.capture());
        var actualSensitiveAuditMessage = auditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }
}
