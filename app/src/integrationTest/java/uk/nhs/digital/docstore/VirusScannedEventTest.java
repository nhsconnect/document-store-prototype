package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.handlers.VirusScannedEventHandler;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.services.VirusScannedEventService;

@ExtendWith(MockitoExtension.class)
public class VirusScannedEventTest extends BaseDocumentStoreTest {

    @Mock Context context;
    VirusScannedEventHandler handler;
    DocumentMetadataStore metadataStore;
    VirusScannedEventService virusScanService;
    JSONObject json;
    DocumentMetadata metadata;
    SNSEvent snsEvent;

    final String INFECTED = "Infected";
    final String CLEAN = "Clean";
    final String BUCKET_NAME = "cool-test-bucket";
    final String KEY = "some-key";

    final String QUARANTINE_BUCKET_NAME = "QuarantineBucket";

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Europe/London"));
        DynamoDBMapper dynamoMapper = new DynamoDBMapper(aws.getDynamoDBClient());
        this.metadataStore = new DocumentMetadataStore(dynamoMapper);
        this.virusScanService =
                new VirusScannedEventService(metadataStore, clock, QUARANTINE_BUCKET_NAME);
        this.handler = new VirusScannedEventHandler(this.virusScanService);
    }

    private void prepare(String result) {

        this.json = new JSONObject();
        this.json.put("bucketName", BUCKET_NAME);
        this.json.put("key", KEY);
        this.json.put("result", result);
        this.json.put("dateScanned", "some-date");

        try {
            metadata =
                    DocumentMetadataBuilder.theMetadata()
                            .withLocation(String.format("s3://%s/%s", BUCKET_NAME, KEY))
                            .build();
        } catch (IllFormedPatientDetailsException e) {
            metadata = new DocumentMetadata();
        }

        metadataStore.save(metadata);

        snsEvent = new SNSEvent();
        SNSEvent.SNS sns = new SNSEvent.SNS();
        sns.setMessage(json.toString());
        SNSEvent.SNSRecord snsRecord = new SNSEvent.SNSRecord();
        snsRecord.setSns(sns);
        List<SNSEvent.SNSRecord> recordList = List.of(snsRecord);
        snsEvent.setRecords(recordList);
    }

    @Test
    public void testInfectedFileVirusScannedResultsAreSavedToMetadata() {

        prepare(INFECTED);
        handler.handleRequest(snsEvent, context);
        DocumentMetadata docMetadata = metadataStore.getById(metadata.getId());
        assert docMetadata.getVirusScanResult().equalsIgnoreCase("infected");
        assert docMetadata.isDocumentUploaded();
        assert docMetadata
                .getLocation()
                .equals(String.format("s3://%s/%s/%s", QUARANTINE_BUCKET_NAME, BUCKET_NAME, KEY));
    }

    @Test
    public void testCleanFileVirusScannedResultsAreSavedToMetadata() {

        prepare(CLEAN);
        handler.handleRequest(snsEvent, context);
        DocumentMetadata docMetadata = metadataStore.getById(metadata.getId());
        assert docMetadata.getVirusScanResult().equalsIgnoreCase("clean");
        assert docMetadata.isDocumentUploaded();
    }
}
