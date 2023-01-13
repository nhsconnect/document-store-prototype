package uk.nhs.digital.docstore.helpers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AwsS3Helper {
    private final AmazonS3 s3Client;

    public AwsS3Helper(AwsClientBuilder.EndpointConfiguration endpointConfiguration) {
        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .enablePathStyleAccess()
                .build();
    }

    public String getDocumentStoreBucketName() {
        var documentBucket = s3Client.listBuckets().stream().filter(bucket -> bucket.getName().startsWith("document-store")).findFirst();

        if (documentBucket.isEmpty()) {
            throw new RuntimeException("Document store bucket not found");
        }

        return documentBucket.get().getName();
    }

    public void emptyBucket(String bucketName) {
        s3Client.listObjects(bucketName)
                .getObjectSummaries()
                .forEach(s3Object -> s3Client.deleteObject(bucketName, s3Object.getKey()));
    }


    public void addDocument(String bucketName, String documentKey, String documentValue) {
        s3Client.putObject(bucketName, documentKey, documentValue);
    }
}
