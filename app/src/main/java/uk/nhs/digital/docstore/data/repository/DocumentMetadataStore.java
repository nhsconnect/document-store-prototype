package uk.nhs.digital.docstore.data.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;
import uk.nhs.digital.docstore.utils.CommonUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentMetadataStore extends DynamoDbConnection {

    public DocumentMetadataStore() {
        super();
    }

    public DocumentMetadataStore(DynamoDBMapper dynamodbMapper) {
        super(dynamodbMapper);
    }

    public DocumentMetadata getById(String id) {
        return mapper.load(DocumentMetadata.class, id);
    }

    public DocumentMetadata getByLocation(String location) {
        List<DocumentMetadata> items = mapper.scan(
                DocumentMetadata.class,
                new DynamoDBScanExpression()
                        .withFilterExpression("#loc = :location")
                        .withExpressionAttributeNames(Map.of("#loc", "Location"))
                        .withExpressionAttributeValues(Map.of(":location", new AttributeValue(location)))
                        .withConsistentRead(false));
        return items.size() > 0 ? items.get(0) : null;
    }

    public List<DocumentMetadata> findByNhsNumber(String nhsNumber) {
        var metadataPaginatedQueryList = mapper.query(
                DocumentMetadata.class,
                new DynamoDBQueryExpression<DocumentMetadata>()
                        .withIndexName("NhsNumberIndex")
                        .withKeyConditionExpression("NhsNumber = :nhsNumber")
                        .withExpressionAttributeValues(Map.of(":nhsNumber", new AttributeValue(nhsNumber)))
                        .withConsistentRead(false)
        );

        return metadataPaginatedQueryList
                .stream()
                .filter(documentMetadata -> documentMetadata.getDeleted() == null)
                .collect(Collectors.toList());
    }

    public DocumentMetadata save(DocumentMetadata documentMetadata) {
        if (documentMetadata.getId() == null) {
            documentMetadata.setId(CommonUtils.generateRandomUUIDString());
        }
        mapper.save(documentMetadata);
        return documentMetadata;
    }

    public List<DocumentMetadata> deleteAndSave(List<DocumentMetadata> documentMetadataList) {
        List<DocumentMetadata> metadataList = new ArrayList<>();
        documentMetadataList.forEach(documentMetadata -> {
                    documentMetadata.setDeleted(Instant.now().toString());
                    metadataList.add(this.save(documentMetadata));
                }
        );
        return metadataList;
    }
}
