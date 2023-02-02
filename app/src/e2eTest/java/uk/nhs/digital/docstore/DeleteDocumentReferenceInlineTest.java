package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.handlers.DeleteDocumentReferenceHandler;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;
import uk.nhs.digital.docstore.helpers.BaseUriHelper;
import uk.nhs.digital.docstore.helpers.DocumentMetadataBuilder;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.services.DocumentDeletionService;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DeleteDocumentReferenceInlineTest {
    @Mock private Context context;
    @Mock private AuditPublisher auditPublisher;

    private DeleteDocumentReferenceHandler deleteDocumentReferenceHandler;

    private DocumentStore documentStore;

    private DocumentMetadataStore documentMetadataStore;
    private static final String AWS_REGION = "eu-west-2";

    @BeforeEach
    void setUp() {
        var apiConfig = new StubbedApiConfig("http://ui-url");
        var endpoint = String.format("http://%s:4566", BaseUriHelper.getAwsHost());
        var endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);
        var bucketName = new AwsS3Helper(endpointConfiguration).getDocumentStoreBucketName();

        var dynamodbClient =
                AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .build();
        var dynamoDBMapper = new DynamoDBMapper(dynamodbClient);

        var s3Client =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(endpointConfiguration)
                        .withPathStyleAccessEnabled(true)
                        .build();

        documentMetadataStore = new DocumentMetadataStore(dynamoDBMapper);
        documentStore = new DocumentStore(s3Client, bucketName);
        var deletionService =
                new DocumentDeletionService(
                        auditPublisher, documentStore, documentMetadataStore, new DocumentMetadataSerialiser());

        deleteDocumentReferenceHandler =
                new DeleteDocumentReferenceHandler(apiConfig, deletionService);
    }

    @Test
    void deletesDocumentAndPublishesAuditMessage() throws JsonProcessingException, IllFormedPatientDetailsException {
        var nhsNumber = new NhsNumber("1234567890");
        var metadata = DocumentMetadataBuilder.theMetadata().withNhsNumber(nhsNumber).build();
        var content = "content of file stored in S3";

        documentMetadataStore.save(metadata);
        documentStore.addDocument(
                new DocumentLocation(metadata.getLocation()).getPath(),
                new ByteArrayInputStream(content.getBytes()));

        deleteDocumentReferenceHandler.handleRequest(createRequestEvent(nhsNumber), context);

        assertThat(documentMetadataStore.findByNhsNumber(nhsNumber)).isEmpty();
        assertThrows(
                AmazonS3Exception.class,
                () -> documentStore.getObjectFromS3(new DocumentLocation(metadata.getLocation())));

        verify(auditPublisher).publish(any(DeletedAllDocumentsAuditMessage.class));
    }

    private APIGatewayProxyRequestEvent createRequestEvent(NhsNumber nhsNumber) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber.getValue());

        return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
    }
}
