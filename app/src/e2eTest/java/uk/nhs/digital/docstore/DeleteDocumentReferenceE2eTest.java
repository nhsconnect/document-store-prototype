package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpClient.newHttpClient;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getAwsHost;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

public class DeleteDocumentReferenceE2eTest {
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";
    private static final String AWS_REGION = "eu-west-2";
    private static final int DEFAULT_PORT = 4566;

    private static final String S3_KEY = "abcd";
    private static final String CODE_VALUE = "185361000000102";
    public static final String TABLE_NAME = "DocumentReferenceMetadata";
    String baseUri = String.format(BASE_URI_TEMPLATE, getAwsHost(), DEFAULT_PORT);
    AwsClientBuilder.EndpointConfiguration awsEndpointConfiguration = new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION);
    AmazonDynamoDB dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(awsEndpointConfiguration)
            .build();

    AwsS3Helper awsS3 = new AwsS3Helper(awsEndpointConfiguration);

    @BeforeEach
    void setUp() {
        // Delete all items from Metadata table before each test
        ScanResult scanResult = dynamoDbClient.scan("DocumentReferenceMetadata", List.of("ID"));
        scanResult
                .getItems()
                .forEach(item -> dynamoDbClient.deleteItem("DocumentReferenceMetadata", item));
    }


    @Test
    void shouldMarkADocumentsRelatedToTheNhsNumberAsDeleted() throws IOException, InterruptedException  {
        var nhsNumber = "1234567890";
        Map<String, AttributeValue> document = Map.of(
                "ID", new AttributeValue("1234"),
                "NhsNumber", new AttributeValue(nhsNumber),
                "Location", new AttributeValue(String.format("s3://%s/%s", awsS3.getDocumentStoreBucketName(), S3_KEY)),
                "ContentType", new AttributeValue("text/plain"),
                "DocumentUploaded", new AttributeValue().withBOOL(true),
                "Description", new AttributeValue("uploaded document"),
                "Created", new AttributeValue("2021-11-04T15:57:30Z"),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE)));

        dynamoDbClient.putItem(TABLE_NAME, document);

        var nhsNumberParameter = URLEncoder.encode("https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber, StandardCharsets.UTF_8);
        var deleteDocumentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference?subject:identifier=" + nhsNumberParameter))
                .DELETE()
                .build();

        var deleteDocumentReferenceResponse = newHttpClient().send(deleteDocumentReferenceRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(deleteDocumentReferenceResponse.statusCode()).isEqualTo(200);

        var actual = dynamoDbClient.getItem(TABLE_NAME, Map.of("ID", document.get("ID")));

        assertThat(actual.getItem().get("DeletedAt")).isNotNull();
    }
}
