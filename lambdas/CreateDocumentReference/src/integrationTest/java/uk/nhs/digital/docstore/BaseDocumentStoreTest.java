package uk.nhs.digital.docstore;

import org.junit.jupiter.api.BeforeEach;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;
import uk.nhs.digital.docstore.helpers.DynamoDBHelper;

public abstract class BaseDocumentStoreTest {
    protected String metadataTableName = "DocumentReferenceMetadata";

    protected String manifestTableName = "DocumentZipTrace";

    protected String documentStoreBucketName;
    protected AWSServiceContainer aws = new AWSServiceContainer();

    @BeforeEach
    void refreshEnvironment() {
        var dynamoDBHelper = new DynamoDBHelper(aws.getDynamoDBClient());
        dynamoDBHelper.refreshTable(metadataTableName);
        dynamoDBHelper.refreshTable(manifestTableName);

        var s3Helper = new AwsS3Helper(aws.getS3Client());
        documentStoreBucketName = s3Helper.getDocumentStoreBucketName();
        s3Helper.emptyBucket(documentStoreBucketName);
    }
}
