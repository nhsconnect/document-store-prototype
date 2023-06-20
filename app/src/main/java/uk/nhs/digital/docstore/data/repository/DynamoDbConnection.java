package uk.nhs.digital.docstore.data.repository;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.IDynamoDBMapper;

public class DynamoDbConnection {
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";
    protected final IDynamoDBMapper mapper;

    public DynamoDbConnection() {
        var dynamodbClient = getDynamodbClient();
        this.mapper =
                new DynamoDBMapper(
                        dynamodbClient,
                        DynamoDBMapperConfig.builder()
                                .withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)
                                .withTableNameResolver(
                                        DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE)
                                .withTableNameOverride(tableNameOverrider())
                                .build());
    }

    public DynamoDbConnection(IDynamoDBMapper dynamodbMapper) {
        this.mapper = dynamodbMapper;
    }

    private AmazonDynamoDB getDynamodbClient() {
        var clientBuilder = AmazonDynamoDBClientBuilder.standard();
        var dynamodbEndpoint = System.getenv("DYNAMODB_ENDPOINT");
        if (!dynamodbEndpoint.equals(DEFAULT_ENDPOINT)) {
            clientBuilder =
                    clientBuilder.withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(
                                    dynamodbEndpoint, AWS_REGION));
        }
        return clientBuilder.build();
    }

    public DynamoDBMapperConfig.TableNameOverride tableNameOverrider() {
        var workspace = System.getenv("WORKSPACE");
        String prefix = workspace != null && !workspace.isEmpty() ? workspace.concat("_") : "";
        return DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(prefix);
    }
}
