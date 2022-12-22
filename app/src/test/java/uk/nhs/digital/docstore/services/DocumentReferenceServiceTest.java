package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.fhir.r4.model.DateTimeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.auditmessages.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.auditmessages.SuccessfulDocumentUploadAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.AuditPublisher;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentReferenceServiceTest {
    @Mock
    private AuditPublisher auditPublisher;
    @Mock
    private DocumentMetadataStore documentMetadataStore;

    @Test
    public void savesDocumentMetadataWithAuditing() throws JsonProcessingException {
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId("1");
        documentMetadata.setNhsNumber("1234");
        documentMetadata.setDescription("Document Title");
        documentMetadata.setContentType("pdf");
        documentMetadata.setCreated(DateTimeType.now().asStringValue());
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher);
        var auditMessage = new CreateDocumentMetadataAuditMessage(documentMetadata);

        when(documentMetadataStore.save(documentMetadata)).thenReturn(documentMetadata);
        var actualDocumentMetadata = documentReferenceService.save(documentMetadata);

        verify(documentMetadataStore).save(documentMetadata);
        verify(auditPublisher).publish(refEq(auditMessage));
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
        var auditMessage = new SuccessfulDocumentUploadAuditMessage(documentMetadata);
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher, now);

        when(documentMetadataStore.getByLocation(location)).thenReturn(documentMetadata);
        documentReferenceService.markDocumentUploaded(location);

        verify(auditPublisher).publish(refEq(auditMessage));
        verify(documentMetadataStore).save(documentMetadata);
        verify(documentMetadataStore).getByLocation(location);
    }
}
