package uk.nhs.digital.docstore.services;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DownloadAllPatientRecordsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.repository.DocumentZipTraceStore;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.NhsNumber;

@ExtendWith(MockitoExtension.class)
class DocumentManifestServiceTest {
    @Mock private SplunkPublisher publisher;
    @Mock private DocumentStore documentStore;
    @Mock private DocumentZipTraceStore zipTraceStore;

    @Captor
    private ArgumentCaptor<DownloadAllPatientRecordsAuditMessage> sensitiveAuditMessageCaptor;

    private DocumentManifestService documentManifestService;

    @BeforeEach
    void setUp() {
        documentManifestService =
                new DocumentManifestService(
                        publisher, zipTraceStore, documentStore, "test-bucket", "1");
    }

    @Test
    void shouldSaveZipInS3AndMetadataInZipTraceDb()
            throws IllFormedPatientDetailsException, MalformedURLException,
                    JsonProcessingException {
        var zipInputStream = new ByteArrayInputStream(new byte[10]);
        var nhsNumber = new NhsNumber("9087654321");
        var presignedUrl = new URL("https://presigned-url-with-filename");
        var fileName = new FileName("Document Title");
        var document =
                DocumentBuilder.baseDocumentBuilder()
                        .withNhsNumber(nhsNumber)
                        .withFileName(fileName)
                        .build();
        var documentList = List.of(document);
        var expectedSensitiveAuditMessage =
                new DownloadAllPatientRecordsAuditMessage(nhsNumber, documentList);

        doNothing().when(zipTraceStore).save(any());
        when(documentStore.generatePreSignedUrlForZip(
                        any(DocumentLocation.class), any(FileName.class)))
                .thenReturn(presignedUrl);

        var actualPresignedUrlString =
                documentManifestService.saveZip(zipInputStream, documentList, nhsNumber);

        assertEquals(presignedUrl.toString(), actualPresignedUrlString);
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
