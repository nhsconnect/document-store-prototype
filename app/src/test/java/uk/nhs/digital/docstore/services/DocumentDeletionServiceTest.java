package uk.nhs.digital.docstore.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder.theMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;
import uk.nhs.digital.docstore.model.NhsNumber;

@ExtendWith(MockitoExtension.class)
class DocumentDeletionServiceTest {
  @Mock AuditPublisher splunkPublisher;
  @Mock DocumentStore documentStore;
  @Mock DocumentMetadataStore metadataStore;
  @Mock DocumentMetadataSerialiser serialiser;

  @Captor private ArgumentCaptor<DeletedAllDocumentsAuditMessage> auditMessageArgumentCaptor;

  private DocumentDeletionService documentDeletionService;

  @BeforeEach
  void setUp() {
    documentDeletionService =
        new DocumentDeletionService(splunkPublisher, documentStore, metadataStore, serialiser);
  }

  @Test
  void shouldDeleteAllDocumentsForPatientAndSendAuditMessage()
      throws IllFormedPatientDetailsException, JsonProcessingException {
    var nhsNumber = new NhsNumber("0123456789");
    var document = DocumentBuilder.baseDocumentBuilder().build();
    var documentList = List.of(document);
    var expectedAuditMessage = new DeletedAllDocumentsAuditMessage(nhsNumber, documentList);
    var metadata = theMetadata().build();
    var metadataList = List.of(metadata);

    when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(metadataList);
    when(metadataStore.deleteAndSave(metadataList)).thenReturn(metadataList);
    when(serialiser.toDocumentModel(metadata)).thenReturn(document);

    documentDeletionService.deleteAllDocumentsForPatient(nhsNumber);

    verify(documentStore).deleteObjectAtLocation(document.getLocation());
    verify(splunkPublisher).publish(auditMessageArgumentCaptor.capture());
    var actualAuditMessage = auditMessageArgumentCaptor.getValue();
    assertThat(actualAuditMessage.getDescription()).isEqualTo("Deleted documents");
    assertThat(actualAuditMessage)
        .usingRecursiveComparison()
        .comparingOnlyFields("nhsNumber", "fileMetadataList")
        .isEqualTo(expectedAuditMessage);
  }
}
