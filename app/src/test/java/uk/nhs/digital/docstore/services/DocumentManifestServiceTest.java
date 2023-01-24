package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentManifestServiceTest {
    @Mock
    private SplunkPublisher publisher;

    @Captor
    private ArgumentCaptor<DownloadAllPatientRecordsAuditMessage> sensitiveAuditMessageCaptor;

    private DocumentManifestService documentManifestService;

    @BeforeEach
    void setUp() {
        documentManifestService = new DocumentManifestService(publisher);
    }

    @Test
    void shouldSendAuditMessage() throws JsonProcessingException, IllFormedPatientDetailsException {
        var nhsNumber = new NhsNumber("9123456780");
        var document = new Document(
                "123",
                nhsNumber,
                "pdf",
                true,
                "Document Title",
                null,
                null,
                null,
                null,
                null
        );

        var documentList = List.of(document);
        var expectedSensitiveAuditMessage = new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentList);

        documentManifestService.audit(nhsNumber, documentList);

        verify(publisher).publish(sensitiveAuditMessageCaptor.capture());

        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }
}