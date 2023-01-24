package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.AuditMessage;
import uk.nhs.digital.docstore.audit.message.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.audit.message.DocumentUploadedAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentReferenceServiceTest {
    @Mock
    private AuditPublisher auditPublisher;
    @Mock
    private DocumentMetadataStore documentMetadataStore;
    @Mock
    private DocumentMetadataSerialiser serialiser;

    @Captor
    private ArgumentCaptor<AuditMessage> auditMessageCaptor;

    @Test
    public void savesDocumentMetadataWithAuditing() throws JsonProcessingException, IllFormedPatientDetailsException {
        var clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId("1");
        documentMetadata.setNhsNumber("1234567890");
        documentMetadata.setDescription("Document Title");
        documentMetadata.setContentType("pdf");
        documentMetadata.setDocumentUploaded(false);
        documentMetadata.setCreated(Instant.now().toString());

        var document = DocumentBuilder.baseDocumentBuilder().build();

        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher, clock, serialiser);
        var expectedSensitiveAuditMessage = new CreateDocumentMetadataAuditMessage(document);

        when(serialiser.fromDocumentModel(document)).thenReturn(documentMetadata);
        when(documentMetadataStore.save(documentMetadata)).thenReturn(documentMetadata);
        when(serialiser.toDocumentModel(documentMetadata)).thenReturn(document);

        var savedDocument = documentReferenceService.save(document);

        verify(auditPublisher).publish(auditMessageCaptor.capture());
        var actualSensitiveAuditMessage = auditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(savedDocument).isEqualTo(document);
    }

    @Test
    public void marksDocumentsUploadedWithAuditing() throws JsonProcessingException, IllFormedPatientDetailsException {
        var location = new DocumentLocation("s3://test/url");
        var clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        var now = Instant.now(clock);
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId("2");
        documentMetadata.setNhsNumber("1234567890");
        documentMetadata.setDescription("Document Title");
        documentMetadata.setContentType("pdf");
        documentMetadata.setIndexed(now.toString());

        var document = DocumentBuilder.baseDocumentBuilder().build();

        var expectedSensitiveAuditMessage = new DocumentUploadedAuditMessage(document);
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher, clock, serialiser);

        when(documentMetadataStore.getByLocation(location)).thenReturn(documentMetadata);
        when(serialiser.toDocumentModel(documentMetadata)).thenReturn(document);
        documentReferenceService.markDocumentUploaded(location);

        verify(auditPublisher).publish(auditMessageCaptor.capture());
        var actualSensitiveAuditMessage = auditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        verify(documentMetadataStore).save(documentMetadata);
        verify(documentMetadataStore).getByLocation(location);
    }

    @Test
    public void doesNotMarkDocumentAsUploadedWhenMetadataIsNull() throws JsonProcessingException, IllFormedPatientDetailsException {
        var location = new DocumentLocation("s3://test/url");
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher, Clock.systemUTC(), serialiser);

        when(documentMetadataStore.getByLocation(location)).thenReturn(null);
        documentReferenceService.markDocumentUploaded(location);

        verify(documentMetadataStore).getByLocation(location);
        verify(auditPublisher, times(0)).publish(any());
        verify(documentMetadataStore, times(0)).save(any());
    }
}
