package uk.nhs.digital.docstore.helpers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;

public class AwsS3Helper {
    private final AmazonS3 s3Client;

    public AwsS3Helper(AwsClientBuilder.EndpointConfiguration endpointConfiguration) {
        s3Client =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .enablePathStyleAccess()
                        .build();
    }

    public AwsS3Helper(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String getDocumentStoreBucketName() {
        var documentBucket =
                s3Client.listBuckets().stream()
                        .filter(bucket -> bucket.getName().equals("test-doc-store"))
                        .findFirst();

        if (documentBucket.isEmpty()) {
            throw new RuntimeException("Document store bucket not found");
        }

        return documentBucket.get().getName();
    }

    public void emptyBucket(String bucketName) {
        s3Client.listObjects(bucketName)
                .getObjectSummaries()
                .forEach(s3Object -> s3Client.deleteObject(bucketName, s3Object.getKey()));

        // Also delete all object versions
        VersionListing versionList =
                s3Client.listVersions(new ListVersionsRequest().withBucketName(bucketName));
        while (true) {
            for (S3VersionSummary vs : versionList.getVersionSummaries()) {
                s3Client.deleteVersion(bucketName, vs.getKey(), vs.getVersionId());
            }

            if (versionList.isTruncated()) {
                versionList = s3Client.listNextBatchOfVersions(versionList);
            } else {
                break;
            }
        }
    }

    public void addDocument(String bucketName, String documentKey, String documentValue) {
        s3Client.putObject(bucketName, documentKey, documentValue);
    }
}
