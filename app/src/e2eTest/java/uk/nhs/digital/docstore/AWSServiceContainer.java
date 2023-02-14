package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AWSServiceContainer {
    private static final String AWS_REGION = "eu-west-2";

    private final AmazonS3 s3Client;
    private final AmazonDynamoDB dynamodbClient;

    public AWSServiceContainer() {
        var endpoint = String.format("http://%s:4566", getAwsHost());

        var endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);
        dynamodbClient =
                AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .build();

        s3Client =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .withPathStyleAccessEnabled(true)
                        .build();
    }

    public AmazonDynamoDB getDynamoDBClient() {
        return dynamodbClient;
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }

    private static String getAwsHost() {
        try {
            InetAddress.getByName("localstack");
            return "localstack";
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }
}
