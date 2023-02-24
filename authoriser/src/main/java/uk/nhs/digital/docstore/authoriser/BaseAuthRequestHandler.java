package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

public class BaseAuthRequestHandler {
    public static final int SEE_OTHER_STATUS_CODE = 303;
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";

    protected static AmazonDynamoDB getDynamodbClient() {
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
}
