package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DocumentReferenceStore {
    private final DynamoDBMapper mapper;
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";

    public DocumentReferenceStore() {
        AmazonDynamoDBClientBuilder clientBuilder = AmazonDynamoDBClientBuilder.standard();
        var dynamodbEndpoint = System.getenv("DYNAMODB_ENDPOINT");
        if (!dynamodbEndpoint.equals(DEFAULT_ENDPOINT)) {
            clientBuilder = clientBuilder.withEndpointConfiguration(new AwsClientBuilder
                    .EndpointConfiguration(dynamodbEndpoint, AWS_REGION));
        }
        AmazonDynamoDB dynamodbClient = clientBuilder.build();
        this.mapper = new DynamoDBMapper(dynamodbClient);
    }

    public DocumentReferenceMetadata getById(String id) {
        return mapper.load(DocumentReferenceMetadata.class, id);
    }
}
