package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class DocumentDeletionServiceTest {
    @Mock
    AuditPublisher splunkPublisher;

    @Captor
    private ArgumentCaptor<DeletedAllDocumentsAuditMessage> auditMessageArgumentCaptor;

    private DocumentDeletionService documentDeletionService;

    @BeforeEach
    void setUp() {
        documentDeletionService = new DocumentDeletionService(splunkPublisher);
    }

    @Test
    void sendsAuditMessage() throws JsonProcessingException, IllFormedPatentDetailsException {
        var nhsNumber = new NhsNumber("0123456789");
        var documentMetadataList = List.of(new DocumentMetadata());
        var expectedAuditMessage = new DeletedAllDocumentsAuditMessage(nhsNumber, documentMetadataList);

        documentDeletionService.audit(nhsNumber, documentMetadataList);

        verify(splunkPublisher).publish(auditMessageArgumentCaptor.capture());
        var actualAuditMessage = auditMessageArgumentCaptor.getValue();
        assertThat(actualAuditMessage.getDescription()).isEqualTo("Deleted documents");
        assertThat(actualAuditMessage)
                .usingRecursiveComparison()
                .comparingOnlyFields("nhsNumber", "fileMetadataList")
                .isEqualTo(expectedAuditMessage);
    }
}