package uk.nhs.digital.docstore.utils;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.util.UUID;

public class CommonUtils {

    public static String generateRandomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public static AmazonS3 buildS3Client(String endpoint, String awsRegion) {
        var clientBuilder = AmazonS3ClientBuilder.standard();
        var s3Endpoint = System.getenv("S3_ENDPOINT");
        boolean s3_use_path_style = "true".equals(System.getenv("S3_USE_PATH_STYLE"));
        if (!s3Endpoint.equals(endpoint)) {
            clientBuilder =
                    clientBuilder
                            .withEndpointConfiguration(
                                    new AwsClientBuilder.EndpointConfiguration(
                                            s3Endpoint, awsRegion))
                            .withPathStyleAccessEnabled(s3_use_path_style);
        }
        return clientBuilder.build();
    }
}
