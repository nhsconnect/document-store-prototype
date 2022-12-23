package uk.nhs.digital.docstore.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.auditmessages.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

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
    void setUp(){
        documentManifestService = new DocumentManifestService(publisher);
    }

    @Test
    void shouldSendAuditMessage() throws JsonProcessingException {
        var nhsNumber = "9123456780";
        var metadata = new DocumentMetadata();
        metadata.setContentType("pdf");
        metadata.setDescription("doc title");
        metadata.setId("123");
        var documentMetadataList = List.of(metadata);
        var expectedSensitiveAuditMessage = new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentMetadataList);

        documentManifestService.audit(nhsNumber, documentMetadataList);

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