package uk.nhs.digital.docstore;

import org.junit.jupiter.api.BeforeEach;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;
import uk.nhs.digital.docstore.helpers.DynamoDBHelper;

public abstract class BaseDocumentStoreTest {
    protected String metadataTableName = "dev_DocumentReferenceMetadata";

    protected String manifestTableName = "dev_DocumentZipTrace";

    protected String documentStoreBucketName;
    protected AWSServiceContainer aws = new AWSServiceContainer();

    protected final DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(aws.getDynamoDBClient());

    @BeforeEach
    void refreshEnvironment() {
        dynamoDBHelper.refreshTable(metadataTableName);
        dynamoDBHelper.refreshTable(manifestTableName);

        var s3Helper = new AwsS3Helper(aws.getS3Client());
        documentStoreBucketName = s3Helper.getDocumentStoreBucketName();
        s3Helper.emptyBucket(documentStoreBucketName);
    }
}
