package uk.nhs.digital.docstore.data.repository;

import uk.nhs.digital.docstore.data.entity.DocumentZipTrace;
import uk.nhs.digital.docstore.utils.CommonUtils;

public class DocumentZipTraceStore extends DynamoDbConnection {

    public DocumentZipTraceStore(String dynamodbEndpoint) {
        super(dynamodbEndpoint);
    }

    public void save(DocumentZipTrace documentTrace) {
        if (documentTrace.getId() == null) {
            documentTrace.setId(CommonUtils.generateRandomUUIDString());
        }
        mapper.save(documentTrace);
    }
}
