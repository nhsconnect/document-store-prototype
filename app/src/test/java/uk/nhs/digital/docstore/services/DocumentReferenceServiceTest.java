package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hl7.fhir.r4.model.DateTimeType;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.auditmessages.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.auditmessages.SuccessfulUploadAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.AuditPublisher;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DocumentReferenceServiceTest {
    @Test
    public void savesDocumentMetadataWithAuditing() throws JsonProcessingException {
        var auditPublisher = mock(AuditPublisher.class);
        var documentMetadataStore = mock(DocumentMetadataStore.class);

        var documentMetadataId = "1";
        var nhsNumber = "1234";
        var documentTitle = "Document Title";
        var contentType = "pdf";
        var now = DateTimeType.now().asStringValue();

        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId(documentMetadataId);
        documentMetadata.setNhsNumber(nhsNumber);
        documentMetadata.setDescription(documentTitle);
        documentMetadata.setContentType(contentType);
        documentMetadata.setCreated(now);

        when(documentMetadataStore.save(documentMetadata)).thenReturn(documentMetadata);

        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher);
        var actualDocumentMetadata = documentReferenceService.save(documentMetadata);
        var auditMessage = new CreateDocumentMetadataAuditMessage(documentMetadata);

        verify(documentMetadataStore).save(documentMetadata);
        verify(auditPublisher).publish(refEq(auditMessage));
        assertThat(actualDocumentMetadata).isEqualTo(documentMetadata);
    }

    @Test
    public void marksDocumentsUploadedWithAuditing() throws JsonProcessingException {
        var auditPublisher = mock(AuditPublisher.class);
        var documentMetadataStore = mock(DocumentMetadataStore.class);

        var documentMetadataId = "2";
        var documentTitle = "Document Title";
        var contentType = "pdf";
        var location = "test.url";
        var now = Instant.now();

        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId(documentMetadataId);
        documentMetadata.setDescription(documentTitle);
        documentMetadata.setContentType(contentType);
        documentMetadata.setIndexed(now.toString());

        when(documentMetadataStore.getByLocation(location)).thenReturn(documentMetadata);

        var auditMessage = new SuccessfulUploadAuditMessage(documentMetadata);
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher, now);

        documentReferenceService.markDocumentUploaded(location);

        verify(auditPublisher).publish(refEq(auditMessage));
        verify(documentMetadataStore).save(documentMetadata);
        verify(documentMetadataStore).getByLocation(location);
    }
}
