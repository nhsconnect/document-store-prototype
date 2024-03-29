package uk.nhs.digital.docstore.data.repository;

import com.amazonaws.HttpMethod;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.FileName;

public class DocumentStore {
    private static final Duration PRE_SIGNED_URL_DURATION = Duration.ofMinutes(30);
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";
    private final AmazonS3 client;

    public DocumentStore() {
        var clientBuilder = AmazonS3ClientBuilder.standard();
        var s3Endpoint = System.getenv("S3_ENDPOINT");
        boolean s3_use_path_style = "true".equals(System.getenv("S3_USE_PATH_STYLE"));
        if (!s3Endpoint.equals(DEFAULT_ENDPOINT)) {
            clientBuilder =
                    clientBuilder
                            .withEndpointConfiguration(
                                    new AwsClientBuilder.EndpointConfiguration(
                                            s3Endpoint, AWS_REGION))
                            .withPathStyleAccessEnabled(s3_use_path_style);
        }
        client = clientBuilder.build();
    }

    public DocumentStore(AmazonS3 client) {
        this.client = client;
    }

    public URL generatePreSignedUrlForZip(DocumentLocation documentLocation, FileName fileName) {
        var generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(
                                documentLocation.getBucketName(), documentLocation.getPath())
                        .withExpiration(getExpirationDate())
                        .withResponseHeaders(
                                new ResponseHeaderOverrides()
                                        .withContentDisposition(
                                                "attachment; fileName=" + fileName.getValue()));

        return client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public URL generatePreSignedUrlForDocument(DocumentLocation documentLocation) {
        var generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(
                                documentLocation.getBucketName(), documentLocation.getPath())
                        .withExpiration(Date.from(Instant.now().plus(PRE_SIGNED_URL_DURATION)))
                        .withMethod(HttpMethod.PUT);

        return client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public S3ObjectInputStream getObjectFromS3(DocumentLocation documentLocation) {
        return client.getObject(documentLocation.getBucketName(), documentLocation.getPath())
                .getObjectContent();
    }

    public void addDocument(DocumentLocation documentLocation, InputStream documentValue) {
        client.putObject(
                documentLocation.getBucketName(),
                documentLocation.getPath(),
                documentValue,
                new ObjectMetadata());
    }

    private Date getExpirationDate() {
        var now = Instant.now();
        var expirationDate = now.plus(PRE_SIGNED_URL_DURATION);
        return Date.from(expirationDate);
    }

    public void deleteObjectAtLocation(DocumentLocation location) {
        client.deleteObject(location.getBucketName(), location.getPath());
    }
}
