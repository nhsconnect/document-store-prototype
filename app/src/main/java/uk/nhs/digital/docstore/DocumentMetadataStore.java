package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;

public class DocumentMetadataStore {
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";

    private final DynamoDBMapper mapper;

    public DocumentMetadataStore() {
        AmazonDynamoDBClientBuilder clientBuilder = AmazonDynamoDBClientBuilder.standard();
        var dynamodbEndpoint = System.getenv("DYNAMODB_ENDPOINT");
        if (!dynamodbEndpoint.equals(DEFAULT_ENDPOINT)) {
            clientBuilder = clientBuilder.withEndpointConfiguration(new AwsClientBuilder
                    .EndpointConfiguration(dynamodbEndpoint, AWS_REGION));
        }
        AmazonDynamoDB dynamodbClient = clientBuilder.build();
        this.mapper = new DynamoDBMapper(
                dynamodbClient,
                DynamoDBMapperConfig.builder()
                        .withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)
                        .build());
    }

    public DocumentMetadata getById(String id) {
        return mapper.load(DocumentMetadata.class, id);
    }

    public DocumentMetadata getByLocation(String location) {
        List<DocumentMetadata> items = mapper.query(
                DocumentMetadata.class,
                new DynamoDBQueryExpression<DocumentMetadata>()
                        .withIndexName("LocationsIndex")
                        .withKeyConditionExpression("#loc = :location")
                        .withExpressionAttributeNames(Map.of("#loc", "Location"))
                        .withExpressionAttributeValues(Map.of(":location", new AttributeValue(location)))
                        .withConsistentRead(false));
        return items.get(0);
    }

    public DocumentMetadata save(DocumentMetadata documentMetadata) {
        if (documentMetadata.getId() == null) {
            documentMetadata.setId(UUID.randomUUID().toString());
        }
        mapper.save(documentMetadata);
        return documentMetadata;
    }
}
