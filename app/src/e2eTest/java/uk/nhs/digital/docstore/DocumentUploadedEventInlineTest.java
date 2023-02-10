package uk.nhs.digital.docstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.handlers.DocumentUploadedEventHandler;
import uk.nhs.digital.docstore.helpers.BaseUriHelper;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.services.DocumentReferenceService;

@ExtendWith(MockitoExtension.class)
public class DocumentUploadedEventInlineTest {
    private static final String AWS_REGION = "eu-west-2";
    @Mock private Context context;
    private DocumentUploadedEventHandler documentUploadedEventHandler;
    private DocumentMetadataStore documentMetadataStore;

    @Mock private SplunkPublisher publisher;

    @BeforeEach
    void setUp() {
        var endpoint = String.format("http://%s:4566", BaseUriHelper.getAwsHost());
        var endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);
        var dynamodbClient =
                AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .build();
        var dynamoDBMapper = new DynamoDBMapper(dynamodbClient);
        documentMetadataStore = new DocumentMetadataStore(dynamoDBMapper);
        var documentReferenceService =
                new DocumentReferenceService(
                        documentMetadataStore, publisher, new DocumentMetadataSerialiser());

        documentUploadedEventHandler = new DocumentUploadedEventHandler(documentReferenceService);
    }

    @Test // Todo test with multiple values
    void markDocumentsAsUploadedAndSendsAuditMessageToSqsWhenThereAreRecords()
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var documentMetadata = DocumentMetadataBuilder.theMetadata().build();
        var documentLocation = new DocumentLocation(documentMetadata.getLocation());
        var size = 12345678L;
        S3EventNotification.UserIdentityEntity userIdentity =
                new S3EventNotification.UserIdentityEntity("some-principalId");
        S3EventNotification.S3BucketEntity s3BucketEntity =
                new S3EventNotification.S3BucketEntity(
                        documentLocation.getBucketName(), userIdentity, "some-arn");
        S3EventNotification.S3ObjectEntity s3ObjectEntity =
                new S3EventNotification.S3ObjectEntity(
                        documentLocation.getPath(),
                        size,
                        "some-eTag",
                        "some-versionId",
                        "sequencer");
        var s3Entity =
                new S3EventNotification.S3Entity(
                        "some-cofigurationId",
                        s3BucketEntity,
                        s3ObjectEntity,
                        "some-schemaVersion");
        S3EventNotification.S3EventNotificationRecord eventRecord =
                new S3EventNotification.S3EventNotificationRecord(
                        "some-region",
                        "some-event",
                        "some-source",
                        null,
                        "some-version",
                        new S3EventNotification.RequestParametersEntity("some-ipaddress"),
                        new S3EventNotification.ResponseElementsEntity("some-elements", "xAmzid2"),
                        s3Entity,
                        new S3EventNotification.UserIdentityEntity("some-principalId"));
        var records = List.of(eventRecord);
        var s3Event = new S3Event(records);

        documentMetadataStore.save(documentMetadata);
        documentUploadedEventHandler.handleRequest(s3Event, context);
        var actual = documentMetadataStore.getByLocation(documentLocation);

        assertThat(actual.isDocumentUploaded()).isTrue(); // Todo add assertion on the index value
        verify(publisher).publish(any(DocumentUploadedAuditMessage.class));
    }
}
