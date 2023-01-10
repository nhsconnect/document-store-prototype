package uk.nhs.digital.docstore.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import org.slf4j.Logger;
import com.amazonaws.services.s3.model.VersionListing;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DeleteDocumentReferenceHandler;

public class DeleteMarkerUtil {
    private static final Logger logger = LoggerFactory.getLogger(DeleteDocumentReferenceHandler.class);
    public static boolean markDocumentAsDelete(AmazonS3 s3client, String s3BucketName) {
        logger.info("inside the markDocumentAsDelete method"+s3client);
        String s3BucketVersioningStatus = s3client.getBucketVersioningConfiguration(s3BucketName).getStatus();
        logger.info("Showing the bucketVersionStatus"+s3BucketVersioningStatus);
        if (!s3BucketVersioningStatus.equals(BucketVersioningConfiguration.ENABLED)) {
            throw new RuntimeException("It is not possible soft delete. As S3 Bucket is not versioning enabled.");
        } else {
            ListVersionsRequest listVersionsRequest = new ListVersionsRequest()
                    .withBucketName(s3BucketName)
                    .withMaxResults(2);
            logger.info("Showing the ListVersionsRequest"+listVersionsRequest);
            VersionListing listVersions = s3client.listVersions(listVersionsRequest);
            logger.info("Showing the VersionListing"+listVersions);
            for (S3VersionSummary versionSummary : listVersions.getVersionSummaries()) {
                logger.info("Showing the S3VersionSummary"+versionSummary);
                if (!versionSummary.isDeleteMarker() && versionSummary.isLatest()) {
                    versionSummary.setIsDeleteMarker(true);
                    return  versionSummary.isDeleteMarker();
                }
            }
        }
        return Boolean.FALSE;
    }

}
