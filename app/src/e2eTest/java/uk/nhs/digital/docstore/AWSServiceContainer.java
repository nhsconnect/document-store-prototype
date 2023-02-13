package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.IDynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import uk.nhs.digital.docstore.helpers.BaseUriHelper;

public class AWSServiceContainer {
    private static final String AWS_REGION = "eu-west-2";
    private IDynamoDBMapper dynamoDBMapper;

    private AmazonS3 s3Client;

    public AWSServiceContainer() {
        var endpoint = String.format("http://%s:4566", BaseUriHelper.getAwsHost());

        var endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);
        var dynamodbClient =
                AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .build();
        dynamoDBMapper = new DynamoDBMapper(dynamodbClient);

        s3Client =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .withPathStyleAccessEnabled(true)
                        .build();
    }

    public IDynamoDBMapper getDynamoDBMapper() {
        return dynamoDBMapper;
    }

    public AmazonS3 getS3Client() {
        return s3Client;
    }
}
