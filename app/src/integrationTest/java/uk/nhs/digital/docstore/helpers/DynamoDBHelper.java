package uk.nhs.digital.docstore.helpers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import java.util.List;

public class DynamoDBHelper {
    private final AmazonDynamoDB dynamoDBClient;

    public DynamoDBHelper(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    public void refreshTable(String tableName) {
        ScanResult scanResult = dynamoDBClient.scan(tableName, List.of("ID"));
        scanResult.getItems().forEach(item -> dynamoDBClient.deleteItem(tableName, item));
    }
}
