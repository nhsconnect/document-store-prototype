package uk.nhs.digital.docstore.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder.theMetadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.message.ReRegistrationAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.events.ReRegistrationEvent;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;
import uk.nhs.digital.docstore.model.NhsNumber;

@ExtendWith(MockitoExtension.class)
class DocumentDeletionServiceTest {
    @Mock AuditPublisher splunkPublisher;
    @Mock DocumentStore documentStore;
    @Mock DocumentMetadataStore metadataStore;
    @Mock DocumentMetadataSerialiser serialiser;

    @Captor
    private ArgumentCaptor<DeletedAllDocumentsAuditMessage>
            deletedDocumentsAuditMessageArgumentCaptor;

    @Captor
    private ArgumentCaptor<ReRegistrationAuditMessage> reRegistrationAuditMessageArgumentCaptor;

    private DocumentDeletionService documentDeletionService;

    @BeforeEach
    void setUp() {
        documentDeletionService =
                new DocumentDeletionService(
                        splunkPublisher, documentStore, metadataStore, serialiser);
    }

    @Test
    void shouldDeleteAllDocumentsForPatient() throws IllFormedPatientDetailsException {
        var nhsNumber = new NhsNumber("0123456789");
        var document = DocumentBuilder.baseDocumentBuilder().build();
        var expectedDocumentList = List.of(document);
        var metadata = theMetadata().build();
        var metadataList = List.of(metadata);

        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(metadataList);
        when(metadataStore.deleteAndSave(metadataList)).thenReturn(metadataList);
        when(serialiser.toDocumentModel(metadata)).thenReturn(document);

        var actualDocumentList = documentDeletionService.deleteAllDocumentsForPatient(nhsNumber);

        verify(documentStore).deleteObjectAtLocation(document.getLocation());
        assertThat(actualDocumentList).usingRecursiveComparison().isEqualTo(expectedDocumentList);
    }

    @Test
    void shouldNotDeleteIfThereAreNoDocumentsAvailableForPatient()
            throws IllFormedPatientDetailsException {
        var nhsNumber = new NhsNumber("9123456789");

        when(metadataStore.findByNhsNumber(nhsNumber)).thenReturn(Collections.emptyList());

        var emptyDocumentList = documentDeletionService.deleteAllDocumentsForPatient(nhsNumber);

        verify(metadataStore, times(0)).deleteAndSave(any());
        verify(documentStore, times(0)).deleteObjectAtLocation(any());
        verify(serialiser, times(0)).toDocumentModel(any());
        assertThat(emptyDocumentList).isEqualTo(Collections.emptyList());
    }

    @Test
    void shouldSendDeleteAllDocumentsAuditMessage()
            throws IllFormedPatientDetailsException, JsonProcessingException {
        var nhsNumber = new NhsNumber("0123456789");
        var document = DocumentBuilder.baseDocumentBuilder().build();
        var documentList = List.of(document);
        var expectedAuditMessage = new DeletedAllDocumentsAuditMessage(nhsNumber, documentList);

        documentDeletionService.deleteAllDocumentsAudit(nhsNumber, documentList);

        verify(splunkPublisher).publish(deletedDocumentsAuditMessageArgumentCaptor.capture());
        var actualAuditMessage = deletedDocumentsAuditMessageArgumentCaptor.getValue();
        assertThat(actualAuditMessage.getDescription()).isEqualTo("Deleted documents");
        assertThat(actualAuditMessage)
                .usingRecursiveComparison()
                .comparingOnlyFields("nhsNumber", "fileMetadataList")
                .isEqualTo(expectedAuditMessage);
    }

    @Test
    void shouldSendReRegistrationAuditMessage()
            throws IllFormedPatientDetailsException, JsonProcessingException {
        var nhsNumber = new NhsNumber("0123456789");
        var document = DocumentBuilder.baseDocumentBuilder().build();
        var documentList = List.of(document);
        var reRegistrationEvent = new ReRegistrationEvent(nhsNumber.getValue(), "nems ID");
        var expectedAuditMessage =
                new ReRegistrationAuditMessage(reRegistrationEvent, documentList);

        documentDeletionService.reRegistrationAudit(reRegistrationEvent, documentList);

        verify(splunkPublisher).publish(reRegistrationAuditMessageArgumentCaptor.capture());
        var actualAuditMessage = reRegistrationAuditMessageArgumentCaptor.getValue();
        assertThat(actualAuditMessage.getDescription())
                .isEqualTo("Deleted documents for re-registered patient");
        assertThat(actualAuditMessage)
                .usingRecursiveComparison()
                .comparingOnlyFields("nhsNumber", "fileMetadataList", "nemsMessageId")
                .isEqualTo(expectedAuditMessage);
    }
}
