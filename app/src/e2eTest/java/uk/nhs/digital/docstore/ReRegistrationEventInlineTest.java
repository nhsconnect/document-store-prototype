package uk.nhs.digital.docstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.ReRegistrationAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.handlers.ReRegistrationEventHandler;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

@ExtendWith(MockitoExtension.class)
public class ReRegistrationEventInlineTest {
    private static final String AWS_REGION = "eu-west-2";
    @Mock private Context context;
    @Mock private SplunkPublisher publisher;
    private DocumentMetadataStore metadataStore;
    private DocumentStore documentStore;
    private DocumentDeletionService deletionService;
    private ReRegistrationEventHandler handler;

    @BeforeEach
    void setUp() {
        var aws = new AWSServiceContainer();
        var bucketName = new AwsS3Helper(aws.getS3Client()).getDocumentStoreBucketName();

        metadataStore = new DocumentMetadataStore(aws.getDynamoDBMapper());
        documentStore = new DocumentStore(aws.getS3Client(), bucketName);
        deletionService =
                new DocumentDeletionService(
                        publisher, documentStore, metadataStore, new DocumentMetadataSerialiser());

        handler = new ReRegistrationEventHandler(deletionService);
    }

    @Test
    void deletePatientDocuments() throws IllFormedPatientDetailsException, JsonProcessingException {
        var nhsNumber = new NhsNumber("9890123456");
        var reRegistrationMessage =
                new JSONObject()
                        .put("nhsNumber", nhsNumber.getValue())
                        .put("newlyRegisteredOdsCode", "TEST123")
                        .put("nemsMessageId", "some id")
                        .put("lastUpdated", "some date")
                        .toString();
        var message =
                new JSONObject()
                        .put("Message", reRegistrationMessage)
                        .put("Timestamp", Instant.now())
                        .toString();
        var sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(message);
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var metadata = DocumentMetadataBuilder.theMetadata().withNhsNumber(nhsNumber).build();
        var content = "content of file stored in S3";

        metadataStore.save(metadata);
        documentStore.addDocument(
                new DocumentLocation(metadata.getLocation()).getPath(),
                new ByteArrayInputStream(content.getBytes()));

        var failedMessages = handler.handleRequest(sqsEvent, context);

        verify(publisher).publish(any(ReRegistrationAuditMessage.class));
        assertTrue(failedMessages.getBatchItemFailures().isEmpty());
        assertThat(metadataStore.findByNhsNumber(nhsNumber)).isEmpty();
        assertThrows(
                AmazonS3Exception.class,
                () -> documentStore.getObjectFromS3(new DocumentLocation(metadata.getLocation())));
    }

    @Test
    void returnsMessageIdWhenDeletionFails() throws JsonProcessingException {
        var messageWithoutNhsNumber =
                "{\"newlyRegisteredOdsCode\":\"N82668\",\"nemsMessageId\":\"34cac591-616c-4727-9d24-c25f97da05e5\",\"lastUpdated\":\"2023-02-03T14:51:43+00:00\"}";
        var sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(messageWithoutNhsNumber);
        var sqsEvent = new SQSEvent();
        sqsEvent.setRecords(List.of(sqsMessage));
        var deletionServiceSpy = spy(deletionService);

        var failedMessages = handler.handleRequest(sqsEvent, context);

        assertThat(failedMessages.getBatchItemFailures().size()).isGreaterThan(0);
        verify(deletionServiceSpy, times(0)).deleteAllDocumentsForPatient(any());
        verify(deletionServiceSpy, times(0)).reRegistrationAudit(any(), any());
    }
}
