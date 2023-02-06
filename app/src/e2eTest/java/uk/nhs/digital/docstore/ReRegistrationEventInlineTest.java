package uk.nhs.digital.docstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import org.json.JSONObject;
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
import uk.nhs.digital.docstore.helpers.BaseUriHelper;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

@ExtendWith(MockitoExtension.class)
public class ReRegistrationEventInlineTest {
    private static final String AWS_REGION = "eu-west-2";
    @Mock private Context context;
    @Mock private SplunkPublisher publisher;

    @Test
    void deletePatientDocuments() throws IllFormedPatientDetailsException, JsonProcessingException {
        var nhsNumber = new NhsNumber("9890123456");
        var endpoint = String.format("http://%s:4566", BaseUriHelper.getAwsHost());
        var endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);
        var bucketName = new AwsS3Helper(endpointConfiguration).getDocumentStoreBucketName();

        var dynamodbClient =
                AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .build();
        var dynamoDBMapper = new DynamoDBMapper(dynamodbClient);

        var s3Client =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .withPathStyleAccessEnabled(true)
                        .build();

        var metadataStore = new DocumentMetadataStore(dynamoDBMapper);
        var documentStore = new DocumentStore(s3Client, bucketName);
        var deletionService =
                new DocumentDeletionService(
                        publisher, documentStore, metadataStore, new DocumentMetadataSerialiser());

        var handler = new ReRegistrationEventHandler(deletionService);
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

        handler.handleRequest(sqsEvent, context);

        verify(publisher).publish(any(ReRegistrationAuditMessage.class));
        assertThat(metadataStore.findByNhsNumber(nhsNumber)).isEmpty();
        assertThrows(
                AmazonS3Exception.class,
                () -> documentStore.getObjectFromS3(new DocumentLocation(metadata.getLocation())));
    }
}
