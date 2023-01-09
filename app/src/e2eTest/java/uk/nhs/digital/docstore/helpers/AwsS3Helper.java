package uk.nhs.digital.docstore.helpers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;

public class AwsS3Helper {
    private AmazonS3 s3Client;

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

    public boolean markDocumentAsDelete(String s3BucketName) {
        String s3BucketVersioningStatus = s3Client.getBucketVersioningConfiguration(s3BucketName).getStatus();
        if (!s3BucketVersioningStatus.equals(BucketVersioningConfiguration.ENABLED)) {
            throw new RuntimeException("It is not possible soft delete. As S3 Bucket is not versioning enabled.");
        } else {
            ListVersionsRequest listVersionsRequest = new ListVersionsRequest()
                    .withBucketName(s3BucketName)
                    .withMaxResults(2);
            VersionListing listVersions = s3Client.listVersions(listVersionsRequest);
            for (S3VersionSummary versionSummary : listVersions.getVersionSummaries()) {
                if (!versionSummary.isDeleteMarker() && versionSummary.isLatest()) {
                    versionSummary.setIsDeleteMarker(true);
                    return  versionSummary.isDeleteMarker();
                }
            }
        }
        return Boolean.FALSE;
    }
}
