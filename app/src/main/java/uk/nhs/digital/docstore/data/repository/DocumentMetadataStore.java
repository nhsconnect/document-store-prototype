package uk.nhs.digital.docstore.data.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.utils.CommonUtils;

import java.util.List;
import java.util.Map;

public class DocumentMetadataStore extends DynamoDbConnection {

    public DocumentMetadataStore(String dynamodbEndpoint) {
        super(dynamodbEndpoint);
    }

    public DocumentMetadata getById(String id) {
        return mapper.load(DocumentMetadata.class, id);
    }

    public DocumentMetadata getByLocation(String location) {
        List<DocumentMetadata> items = mapper.query(
                DocumentMetadata.class,
                new DynamoDBQueryExpression<DocumentMetadata>()
                        .withIndexName("LocationsIndex")
                        .withKeyConditionExpression("#loc = :location")
                        .withExpressionAttributeNames(Map.of("#loc", "Location"))
                        .withExpressionAttributeValues(Map.of(":location", new AttributeValue(location)))
                        .withConsistentRead(false));
        return items.get(0);
    }

    public List<DocumentMetadata> findByNhsNumber(String nhsNumber) {
        return mapper.query(
                DocumentMetadata.class,
                new DynamoDBQueryExpression<DocumentMetadata>()
                        .withIndexName("NhsNumberIndex")
                        .withKeyConditionExpression("NhsNumber = :nhsNumber")
                        .withExpressionAttributeValues(Map.of(":nhsNumber", new AttributeValue(nhsNumber)))
                        .withConsistentRead(false));
    }

    public DocumentMetadata save(DocumentMetadata documentMetadata) {
        if (documentMetadata.getId() == null) {
            documentMetadata.setId(CommonUtils.generateRandomUUIDString());
        }
        mapper.save(documentMetadata);
        return documentMetadata;
    }
}
