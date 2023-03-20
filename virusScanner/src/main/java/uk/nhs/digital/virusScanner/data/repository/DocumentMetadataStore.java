package uk.nhs.digital.virusScanner.data.repository;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.IDynamoDBMapper;
import uk.nhs.digital.virusScanner.data.entity.DocumentMetadata;

public class DocumentMetadataStore {

    private final IDynamoDBMapper mapper;

    public DocumentMetadataStore() {
        AmazonDynamoDB standardDynamoDbClient = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDBMapperConfig mapperConfig =
                DynamoDBMapperConfig.builder()
                        .withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)
                        .build();
        this.mapper = new DynamoDBMapper(standardDynamoDbClient, mapperConfig);
    }

    public void save(DocumentMetadata documentMetadata) {
        mapper.save(documentMetadata);
    }
}
