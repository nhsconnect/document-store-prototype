package uk.nhs.digital.docstore;

import static com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.DeletedAllDocumentsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.handlers.DeleteDocumentReferenceHandler;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

@ExtendWith(MockitoExtension.class)
public class DeleteDocumentReferenceInlineTest {
    @Mock private Context context;
    @Mock private AuditPublisher auditPublisher;

    private DeleteDocumentReferenceHandler deleteDocumentReferenceHandler;
    private static final String AWS_ENDPOINT = "http://localhost:4566";
    private static final String AWS_REGION = "eu-west-2";

    @BeforeEach
    void setUp() {
        var apiConfig = new StubbedApiConfig("http://ui-url");
        var documentDeletionService =
                new DocumentDeletionService(
                        auditPublisher,
                        // TODO: Change bucket name
                        new DocumentStore(
                                AmazonS3ClientBuilder.standard()
                                        .withEndpointConfiguration(
                                                new AwsClientBuilder.EndpointConfiguration(
                                                        AWS_ENDPOINT, AWS_REGION))
                                        .withPathStyleAccessEnabled(true)
                                        .build(),
                                "bucket-name"),
                        new DocumentMetadataStore(
                                new DynamoDBMapper(
                                        AmazonDynamoDBClientBuilder.standard()
                                                .withEndpointConfiguration(
                                                        new AwsClientBuilder.EndpointConfiguration(
                                                                AWS_ENDPOINT, AWS_REGION))
                                                .build(),
                                        DynamoDBMapperConfig.builder()
                                                .withSaveBehavior(UPDATE_SKIP_NULL_ATTRIBUTES)
                                                .build())),
                        new DocumentMetadataSerialiser());

        deleteDocumentReferenceHandler =
                new DeleteDocumentReferenceHandler(apiConfig, documentDeletionService);
    }

    // TODO: Use DocumentMetadataStore and DocumentStore to interact with AWS resources so that we
    // can
    // perform arrange and assert + change test name
    @Test
    void sendsAuditMessageUponSuccessfulDeletion() throws JsonProcessingException {

        deleteDocumentReferenceHandler.handleRequest(createRequestEvent(), context);

        verify(auditPublisher).publish(any(DeletedAllDocumentsAuditMessage.class));
    }

    private APIGatewayProxyRequestEvent createRequestEvent() {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000009");

        return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
    }
}
