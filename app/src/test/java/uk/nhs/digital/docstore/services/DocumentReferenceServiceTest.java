package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.auditmessages.CreateDocumentMetadataAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.AuditPublisher;

import java.time.Instant;
import java.util.List;

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
        var now = Instant.now();

        var documentMetadata = new DocumentMetadata();
        documentMetadata.setId(documentMetadataId);
        documentMetadata.setNhsNumber(nhsNumber);
        documentMetadata.setDescription(documentTitle);
        documentMetadata.setType(List.of(contentType));
        documentMetadata.setCreated(now.toString());

        when(documentMetadataStore.save(documentMetadata)).thenReturn(documentMetadata);

        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher);
        var actualDocumentMetadata = documentReferenceService.save(documentMetadata);

        verify(documentMetadataStore).save(documentMetadata);

        var auditMessage = new CreateDocumentMetadataAuditMessage(documentMetadata);
        verify(auditPublisher).publish(refEq(auditMessage));

        assertThat(actualDocumentMetadata).isEqualTo(documentMetadata);
    }
}
