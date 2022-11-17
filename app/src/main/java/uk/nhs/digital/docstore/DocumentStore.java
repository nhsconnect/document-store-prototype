package uk.nhs.digital.docstore;

import com.amazonaws.HttpMethod;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class DocumentStore {
    private static final Duration PRE_SIGNED_URL_DURATION = Duration.ofSeconds(60);
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";
    private final String bucketName;

    private final AmazonS3 client;

    public DocumentStore(String bucketName) {
        var clientBuilder = AmazonS3ClientBuilder.standard();
        var s3Endpoint = System.getenv("S3_ENDPOINT");
        boolean s3_use_path_style = "true".equals(System.getenv("S3_USE_PATH_STYLE"));
        if (!s3Endpoint.equals(DEFAULT_ENDPOINT)) {
            clientBuilder = clientBuilder
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Endpoint, AWS_REGION))
                    .withPathStyleAccessEnabled(s3_use_path_style);
        }
        client = clientBuilder.build();
        this.bucketName = bucketName;
    }

    public URL generatePreSignedUrl(DocumentDescriptor descriptor) {
        return client.generatePresignedUrl(descriptor.bucket, descriptor.path, getExpirationDate());
    }

    public URL generatePreSignedUrlForZip(DocumentDescriptor descriptor, String filename) {
        var generatePresignedUrlRequest = new GeneratePresignedUrlRequest(descriptor.bucket, descriptor.path)
                .withExpiration(getExpirationDate())
                .withResponseHeaders(new ResponseHeaderOverrides().withContentDisposition("attachment; filename="+filename));
        return client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public DocumentDescriptorAndURL generateDocumentDescriptorAndURL() {
        String key = UUID.randomUUID().toString();
        URL url = client.generatePresignedUrl(bucketName, key, getExpirationDate(), HttpMethod.PUT);
        return new DocumentDescriptorAndURL(new DocumentDescriptor(bucketName, key), url);
    }

    public S3ObjectInputStream getObjectFromS3(DocumentMetadata metadata) {
        return client.getObject(bucketName, DocumentDescriptor.from(metadata).getPath()).getObjectContent();
    }

    public void addDocument(String documentKey, InputStream documentValue) {
        client.putObject(bucketName, documentKey, documentValue, new ObjectMetadata());
    }

    private Date getExpirationDate() {
        var now = Instant.now();
        var expirationDate = now.plus(PRE_SIGNED_URL_DURATION);
        return Date.from(expirationDate);
    }

    public static class DocumentDescriptor {
        private final String bucket;
        private final String path;

        public DocumentDescriptor(String bucket, String path) {
            this.bucket = bucket;
            this.path = path;
        }

        public String toLocation() {
            return "s3://" + bucket + "/" + path;
        }

        public String getPath() {
            return path;
        }

        public static DocumentDescriptor from(DocumentMetadata metadata) {
            URI location = URI.create(metadata.getLocation());
            return new DocumentDescriptor(location.getHost(), location.getPath().substring(1));
        }
    }

    public static class DocumentDescriptorAndURL {
        private final DocumentDescriptor documentDescriptor;
        private final URL documentUrl;

        public DocumentDescriptorAndURL(DocumentDescriptor documentDescriptor, URL documentUrl) {
            this.documentDescriptor = documentDescriptor;
            this.documentUrl = documentUrl;
        }

        public String getDocumentUrl() {
            return documentUrl.toString();
        }

        public DocumentDescriptor getDocumentDescriptor() {
            return documentDescriptor;
        }
    }
}
