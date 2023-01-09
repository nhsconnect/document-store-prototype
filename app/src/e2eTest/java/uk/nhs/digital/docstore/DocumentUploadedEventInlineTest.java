package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.auditmessages.DocumentUploadedAuditMessage;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.AuditPublisher;
import uk.nhs.digital.docstore.services.DocumentReferenceService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    private AuditPublisher auditPublisher;

    private DocumentUploadedEventHandler documentUploadedEventHandler;

    @BeforeEach
    void setUp() {
        var documentReferenceService = new DocumentReferenceService(documentMetadataStore, auditPublisher);

        documentUploadedEventHandler = new DocumentUploadedEventHandler(documentReferenceService);
    }

    @Test
    void sendsAuditMessageToSqsWhenThereAreRecords() throws JsonProcessingException {
        var id = "some-id";
        var fileName = "some-file-name";
        var fileType = "some-file-type";
        var nhsNumber = "1234567890";

        when(s3Event.getRecords()).thenReturn(List.of(s3EventNotificationRecord));
        when(s3EventNotificationRecord.getS3()).thenReturn(s3Entity);
        when(s3Entity.getBucket()).thenReturn(s3BucketEntity);
        when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
        when(documentMetadataStore.getByLocation(any())).thenReturn(createMetadata(id, nhsNumber, fileName, fileType));
        documentUploadedEventHandler.handleRequest(s3Event, context);

        verify(auditPublisher).publish(any(DocumentUploadedAuditMessage.class));
    }

    private DocumentMetadata createMetadata(String id, String nhsNumber, String fileName, String fileType) {
        var metadata = new DocumentMetadata();
        metadata.setId(id);
        metadata.setNhsNumber(nhsNumber);
        metadata.setDescription(fileName);
        metadata.setContentType(fileType);
        return metadata;
    }
}
