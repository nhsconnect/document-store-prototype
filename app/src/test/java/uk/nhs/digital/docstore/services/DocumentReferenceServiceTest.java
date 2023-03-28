package uk.nhs.digital.docstore.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.AuditMessage;
import uk.nhs.digital.docstore.audit.message.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;

@ExtendWith(MockitoExtension.class)
public class DocumentReferenceServiceTest {
    @Mock private AuditPublisher auditPublisher;
    @Mock private DocumentMetadataStore documentMetadataStore;
    @Mock private DocumentMetadataSerialiser serialiser;

    @Captor private ArgumentCaptor<AuditMessage> auditMessageCaptor;

    @Test
    public void savesDocumentMetadataWithAuditing()
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId("1");
        documentMetadata.setNhsNumber("1234567890");
        documentMetadata.setFileName("Document Title");
        documentMetadata.setContentType("pdf");
        documentMetadata.setDocumentUploaded(false);
        documentMetadata.setCreated(Instant.now().toString());

        var document = DocumentBuilder.baseDocumentBuilder().build();

        var documentReferenceService =
                new DocumentReferenceService(documentMetadataStore, auditPublisher, serialiser);
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
}
