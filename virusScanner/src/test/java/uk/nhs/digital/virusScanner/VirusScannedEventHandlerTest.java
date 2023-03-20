package uk.nhs.digital.virusScanner;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class VirusScannedEventHandlerTest {

    @Mock DynamoDB dynamoDb;

    @Test
    public void testSavesVirusScanResultsInDynamoDb() {}
}
