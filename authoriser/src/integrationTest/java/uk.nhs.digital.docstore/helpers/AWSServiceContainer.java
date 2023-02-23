package uk.nhs.digital.docstore.helpers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

public class AWSServiceContainer {
    private static final String AWS_REGION = "eu-west-2";

    private final AmazonDynamoDB dynamodbClient;

    public AWSServiceContainer() {
        var endpoint = System.getenv("AWS_ENDPOINT");

        System.out.println("AWS endpoint set to " + endpoint);

        var dynamoDBClientBuilder = AmazonDynamoDBClientBuilder.standard();

        if (endpoint != null) {
            var endpointConfiguration =
                    new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);

            dynamoDBClientBuilder =
                    dynamoDBClientBuilder.withEndpointConfiguration(endpointConfiguration);
            ;
        }

        dynamodbClient = dynamoDBClientBuilder.build();
    }

    public AmazonDynamoDB getDynamoDBClient() {
        return dynamodbClient;
    }
}
