package uk.nhs.digital.docstore.utils;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

    public static String decryptKey(String envVariable) {
        AWSKMS kmsClient = AWSKMSClientBuilder.standard().build();
        String ciphertext = System.getenv(envVariable);

        byte[] encryptedBytes = java.util.Base64.getDecoder().decode(ciphertext);
        ByteBuffer byteBuffer = ByteBuffer.allocate(encryptedBytes.length);
        byteBuffer.put(encryptedBytes);
        byteBuffer.flip();

        DecryptRequest decryptRequest = new DecryptRequest().withCiphertextBlob(byteBuffer);
        DecryptResult decryptResult = kmsClient.decrypt(decryptRequest);

        ByteBuffer decryptedByteBuffer = decryptResult.getPlaintext();
        return StandardCharsets.UTF_8.decode(decryptedByteBuffer).toString();
    }
}
