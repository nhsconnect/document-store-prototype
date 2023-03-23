package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.of("Europe/London"));
        DynamoDBMapper dynamoMapper = new DynamoDBMapper(aws.getDynamoDBClient());
        this.metadataStore = new DocumentMetadataStore(dynamoMapper);
        this.virusScanService = new VirusScannedEventService(metadataStore, clock);
        this.handler = new VirusScannedEventHandler(this.virusScanService);

        String bucketName = "cool-test-bucket";
        String key = "some-key";
        String result = "Infected";

        this.json = new JSONObject();
        this.json.put("bucketName", bucketName);
        this.json.put("key", key);
        this.json.put("result", result);
        this.json.put("dateScanned", "some-date");

        try {
            metadata =
                    DocumentMetadataBuilder.theMetadata()
                            .withLocation(String.format("s3://%s/%s", bucketName, key))
                            .build();
        } catch (IllFormedPatientDetailsException e) {
            metadata = new DocumentMetadata();
        }

        metadataStore.save(metadata);
    }

    @Test
    public void testInfectedFileVirusScannedResultsAreSavedToMetadata() {}

    @Test
    public void testCleanFileVirusScannedResultsAreSavedToMetadata() {}
}
