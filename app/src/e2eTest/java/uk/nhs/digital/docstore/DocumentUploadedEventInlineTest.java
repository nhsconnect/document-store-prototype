package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;
import uk.nhs.digital.docstore.services.DocumentReferenceService;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
public class DocumentUploadedEventInlineTest {
    @Mock
    private Context context;
    @Mock
    private S3Event s3Event;
    @Mock
    private S3EventNotification.S3EventNotificationRecord s3EventNotificationRecord;
    @Mock
    private S3EventNotification.S3Entity s3Entity;
    @Mock
    private S3EventNotification.S3BucketEntity s3BucketEntity;
    @Mock
    private S3EventNotification.S3ObjectEntity s3ObjectEntity;
    @Mock
    private DocumentMetadataStore documentMetadataStore;
    @Mock
    private AmazonSQS amazonSqsClient;

    @Captor
    ArgumentCaptor<SendMessageRequest> messageRequestCaptor;

    @SystemStub
    private EnvironmentVariables environmentVariables;

    private DocumentUploadedEventHandler documentUploadedEventHandler;

    @BeforeEach
    void setUp() {
        var documentReferenceService = new DocumentReferenceService(
                documentMetadataStore,
                new SplunkPublisher(amazonSqsClient)
        );

        documentUploadedEventHandler = new DocumentUploadedEventHandler(documentReferenceService);
    }

    @Test
    void sendsAuditMessageToSqsWhenThereAreRecords() {
        var expectedFileMetadata = new JSONObject();
        var id = "some-id";
        var fileName = "some-file-name";
        var fileType = "some-file-type";
        expectedFileMetadata.put("id", id);
        expectedFileMetadata.put("fileName", fileName);
        expectedFileMetadata.put("fileType", fileType);
        var now = Instant.now();
        var expectedMessageBody = new JSONObject();
        expectedMessageBody.put("fileMetadata", expectedFileMetadata);
        expectedMessageBody.put("timestamp", now.toString());

        environmentVariables.set("SQS_QUEUE_URL", "document-store-audit-queue-url");
        when(s3Event.getRecords()).thenReturn(List.of(s3EventNotificationRecord));
        when(s3EventNotificationRecord.getS3()).thenReturn(s3Entity);
        when(s3Entity.getBucket()).thenReturn(s3BucketEntity);
        when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
        when(documentMetadataStore.getByLocation(any())).thenReturn(createMetadata(id, fileName, fileType));
        documentUploadedEventHandler.handleRequest(s3Event, context);

        verify(amazonSqsClient).sendMessage(messageRequestCaptor.capture());
        var messageBody = messageRequestCaptor.getValue().getMessageBody();
        var timestamp = JsonPath.read(messageBody, "$.timestamp").toString();
        assertThatJson(messageBody).whenIgnoringPaths("timestamp").isEqualTo(expectedMessageBody);
        assertThat(Instant.parse(timestamp)).isCloseTo(now, within(1, ChronoUnit.SECONDS));
    }

    @Test
    void doesNotSendAuditMessageToSqsWhenThereAreNoRecords() {
        when(s3Event.getRecords()).thenReturn(Collections.emptyList());
        documentUploadedEventHandler.handleRequest(s3Event, context);

        verify(amazonSqsClient, never()).sendMessage(any());
    }

    private DocumentMetadata createMetadata(String id, String fileName, String fileType) {
        var metadata = new DocumentMetadata();
        metadata.setId(id);
        metadata.setDescription(fileName);
        metadata.setContentType(fileType);
        return metadata;
    }
}
