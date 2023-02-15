package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AWSServiceContainer {
    private static final String AWS_REGION = "eu-west-2";

    private final AmazonS3 s3Client;
    private final AmazonDynamoDB dynamodbClient;

    public AWSServiceContainer() {
        var endpoint = System.getenv("AWS_ENDPOINT");

        System.out.println("AWS endpoint set to " + endpoint);

        var dynamoDBClientBuilder = AmazonDynamoDBClientBuilder.standard();
        var s3ClientBuilder = AmazonS3ClientBuilder.standard();

        if (endpoint != null) {
            var endpointConfiguration =
                    new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);

            dynamoDBClientBuilder =
                    dynamoDBClientBuilder.withEndpointConfiguration(endpointConfiguration);
            s3ClientBuilder =
                    s3ClientBuilder
                            .withEndpointConfiguration(endpointConfiguration)
                            .withPathStyleAccessEnabled(true);
        }

        dynamodbClient = dynamoDBClientBuilder.build();
        s3Client = s3ClientBuilder.build();
    }

    public AmazonDynamoDB getDynamoDBClient() {
        return dynamodbClient;
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }
}
