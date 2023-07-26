package uk.nhs.digital.docstore.helpers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import java.util.List;

public class DynamoDBHelper {
    private final AmazonDynamoDB dynamoDBClient;

    protected DynamoDBMapperConfig config;

    public DynamoDBHelper(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
        this.config = DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix("dev_").config();
    }

    public void refreshTable(String tableName) {
        ScanResult scanResult = dynamoDBClient.scan(tableName, List.of("ID"));
        scanResult.getItems().forEach(item -> dynamoDBClient.deleteItem(tableName, item));
    }

    public DynamoDBMapper getMapper() {
        return new DynamoDBMapper(dynamoDBClient, config);
    }
}
