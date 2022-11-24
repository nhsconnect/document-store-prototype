package uk.nhs.digital.docstore.data.repository;

import uk.nhs.digital.docstore.data.entity.DocumentTrace;
import uk.nhs.digital.docstore.utils.CommonUtils;

public class DocumentTraceStore extends DynamoDbConnection {

    public void save(DocumentTrace documentTrace) {
        if (documentTrace.getId() == null) {
            documentTrace.setId(CommonUtils.generateRandomUUIDString());
        }
        mapper.save(documentTrace);
    }
}
