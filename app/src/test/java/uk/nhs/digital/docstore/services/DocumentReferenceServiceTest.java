package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.fhir.r4.model.DateTimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.AuditMessage;
import uk.nhs.digital.docstore.audit.message.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.audit.message.DocumentUploadedAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;

import java.time.Instant;
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

    @Captor
    private ArgumentCaptor<AuditMessage> auditMessageCaptor;

    @Test
    public void savesDocumentMetadataWithAuditing() throws JsonProcessingException {
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId("1");
        documentMetadata.setNhsNumber("1234");
        documentMetadata.setDescription("Document Title");
        documentMetadata.setContentType("pdf");
        documentMetadata.setCreated(DateTimeType.now().asStringValue());
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher);
        var expectedSensitiveAuditMessage = new CreateDocumentMetadataAuditMessage(documentMetadata);

        when(documentMetadataStore.save(documentMetadata)).thenReturn(documentMetadata);
        var actualDocumentMetadata = documentReferenceService.save(documentMetadata);

        verify(documentMetadataStore).save(documentMetadata);
        verify(auditPublisher).publish(auditMessageCaptor.capture());
        var actualSensitiveAuditMessage = auditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(actualDocumentMetadata).isEqualTo(documentMetadata);
    }

    @Test
    public void marksDocumentsUploadedWithAuditing() throws JsonProcessingException {
        var location = "test.url";
        var now = Instant.now();
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId("2");
        documentMetadata.setDescription("Document Title");
        documentMetadata.setContentType("pdf");
        documentMetadata.setIndexed(now.toString());
        var expectedSensitiveAuditMessage = new DocumentUploadedAuditMessage(documentMetadata);
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher, now);

        when(documentMetadataStore.getByLocation(location)).thenReturn(documentMetadata);
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
    public void doesNotMarkDocumentAsUploadedWhenMetadataIsNull() throws JsonProcessingException {
        var location = "test.url";
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher, Instant.now());

        when(documentMetadataStore.getByLocation(location)).thenReturn(null);
        documentReferenceService.markDocumentUploaded(location);

        verify(documentMetadataStore).getByLocation(location);
        verify(auditPublisher, times(0)).publish(any());
        verify(documentMetadataStore, times(0)).save(any());
    }
}
