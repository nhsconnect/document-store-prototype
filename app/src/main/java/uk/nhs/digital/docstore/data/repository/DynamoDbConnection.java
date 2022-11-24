package uk.nhs.digital.docstore.data.repository;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;

public class DynamoDbConnection {
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";

    protected final DynamoDBMapper mapper;

    public DynamoDbConnection() {
        var dynamodbClient = getDynamodbClient();
        this.mapper = new DynamoDBMapper(
                dynamodbClient,
                DynamoDBMapperConfig.builder()
                        .withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)
                        .build());
    }

    private AmazonDynamoDB getDynamodbClient() {
        var clientBuilder = AmazonDynamoDBClientBuilder.standard();
        var dynamodbEndpoint = System.getenv("DYNAMODB_ENDPOINT");
        if (!dynamodbEndpoint.equals(DEFAULT_ENDPOINT)) {
            clientBuilder = clientBuilder.withEndpointConfiguration(new AwsClientBuilder
                    .EndpointConfiguration(dynamodbEndpoint, AWS_REGION));
        }
        var dynamodbClient = clientBuilder.build();
        return dynamodbClient;
    }
}
