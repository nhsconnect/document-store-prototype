package uk.nhs.digital.docstore.data.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.IDynamoDBMapper;
import uk.nhs.digital.docstore.data.entity.DocumentZipTrace;
import uk.nhs.digital.docstore.utils.CommonUtils;

public class DocumentZipTraceStore extends DynamoDbConnection {

    public DocumentZipTraceStore() {
        super();
    }

    public DocumentZipTraceStore(IDynamoDBMapper dynamodbMapper) {
        super(dynamodbMapper);
    }

    public void save(DocumentZipTrace documentTrace) {
        if (documentTrace.getId() == null) {
            documentTrace.setId(CommonUtils.generateRandomUUIDString());
        }
        mapper.save(documentTrace);
    }
}
