package uk.nhs.digital.docstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.VirusScannedAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.lambdas.FakeVirusScannedEventHandler;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.services.VirusScannedEventService;

@ExtendWith(MockitoExtension.class)
public class FakeVirusScannedEventTest extends BaseDocumentStoreTest {
    private static final String QUARANTINE_BUCKET_NAME = "quarantine-test-bucket";
    @Mock private Context context;
    private FakeVirusScannedEventHandler fakeVirusScannedEventHandler;
    private DocumentMetadataStore documentMetadataStore;
    private final Clock clock = Clock.fixed(Instant.now(), TimeZone.getDefault().toZoneId());

    @Mock private SplunkPublisher publisher;

    @BeforeEach
    void setUp() {
        documentMetadataStore = new DocumentMetadataStore(dynamoDBHelper.getMapper());
        var virusScannedEventService =
                new VirusScannedEventService(
                        documentMetadataStore,
                        clock,
                        QUARANTINE_BUCKET_NAME,
                        publisher,
                        new DocumentMetadataSerialiser());

        fakeVirusScannedEventHandler = new FakeVirusScannedEventHandler(virusScannedEventService);
    }

    @Test
    void markDocumentsAsUploadedAndSendsAuditMessageToSqsWhenThereAreRecords()
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var documentMetadata = DocumentMetadataBuilder.theMetadata().build();
        var documentLocation = new DocumentLocation(documentMetadata.getLocation());
        documentMetadataStore.save(documentMetadata);

        fakeVirusScannedEventHandler.handleRequest(
                makeS3EventNotification(documentLocation), context);
        var actual = documentMetadataStore.getByLocation(documentLocation);

        assertThat(actual.isDocumentUploaded()).isTrue();
        assertThat(actual.getIndexed()).isEqualTo(Instant.now(clock).toString());
        verify(publisher).publish(any(VirusScannedAuditMessage.class));
    }

    private S3Event makeS3EventNotification(DocumentLocation documentLocation) {
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
        return new S3Event(records);
    }
}
