package uk.nhs.digital.docstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder.theMetadata;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DocumentUploadedAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.handlers.DocumentUploadedEventHandler;
import uk.nhs.digital.docstore.services.DocumentReferenceService;

@ExtendWith(MockitoExtension.class)
public class DocumentUploadedEventInlineTest {
    @Mock private Context context;
    @Mock private S3Event s3Event;
    @Mock private S3EventNotification.S3EventNotificationRecord s3EventNotificationRecord;
    @Mock private S3EventNotification.S3Entity s3Entity;
    @Mock private S3EventNotification.S3BucketEntity s3BucketEntity;
    @Mock private S3EventNotification.S3ObjectEntity s3ObjectEntity;
    @Mock private DocumentMetadataStore documentMetadataStore;
    @Mock private AuditPublisher auditPublisher;

    private DocumentUploadedEventHandler documentUploadedEventHandler;

    @BeforeEach
    void setUp() {
        var documentReferenceService =
                new DocumentReferenceService(
                        documentMetadataStore, auditPublisher, new DocumentMetadataSerialiser());

        documentUploadedEventHandler = new DocumentUploadedEventHandler(documentReferenceService);
    }

    @Test
    void sendsAuditMessageToSqsWhenThereAreRecords()
            throws JsonProcessingException, IllFormedPatientDetailsException {

        when(s3Event.getRecords()).thenReturn(List.of(s3EventNotificationRecord));
        when(s3EventNotificationRecord.getS3()).thenReturn(s3Entity);
        when(s3Entity.getBucket()).thenReturn(s3BucketEntity);
        when(s3Entity.getObject()).thenReturn(s3ObjectEntity);
        when(documentMetadataStore.getByLocation(any())).thenReturn(theMetadata().build());
        documentUploadedEventHandler.handleRequest(s3Event, context);

        verify(auditPublisher).publish(any(DocumentUploadedAuditMessage.class));
    }
}
