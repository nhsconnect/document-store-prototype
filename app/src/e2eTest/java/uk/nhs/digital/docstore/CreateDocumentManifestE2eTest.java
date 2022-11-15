package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.auth0.jwt.JWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static com.auth0.jwt.algorithms.Algorithm.none;
import static java.net.http.HttpClient.newHttpClient;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.resolveContainerHost;

public class CreateDocumentManifestE2eTest {
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";
    private static final String AWS_REGION = "eu-west-2";
    private static final int DEFAULT_PORT = 4566;
    private static final String S3_KEY = "abcd";
    private static final String S3_VALUE = "content";
    private static final String S3_KEY2 = "btyu";
    private static final String S3_VALUE2 = "another content";
    private static final String CODE_VALUE = "185361000000102";
    private final String baseUri = String.format(BASE_URI_TEMPLATE, getAwsHost(), DEFAULT_PORT);
    private final AwsClientBuilder.EndpointConfiguration awsEndpointConfiguration = new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION);
    private final AwsS3Helper aws = new AwsS3Helper(awsEndpointConfiguration);

    @BeforeEach
    void setUp() {
        AmazonDynamoDB dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(awsEndpointConfiguration)
                .build();
        ScanResult scanResult = dynamoDbClient.scan("DocumentReferenceMetadata", List.of("ID"));
        scanResult.getItems()
                .forEach(item -> dynamoDbClient.deleteItem("DocumentReferenceMetadata", item));

        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("1234"),
                "NhsNumber", new AttributeValue("9000000009"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), S3_KEY)),
                "ContentType", new AttributeValue("text/plain"),
                "DocumentUploaded", new AttributeValue().withBOOL(true),
                "Description", new AttributeValue("uploaded document"),
                "Created", new AttributeValue("2021-11-04T15:57:30Z"),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));
        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("6987"),
                "NhsNumber", new AttributeValue("9000000009"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), S3_KEY2)),
                "ContentType", new AttributeValue("text/plain"),
                "DocumentUploaded", new AttributeValue().withBOOL(true),
                "Description", new AttributeValue("uploaded document 2"),
                "Created", new AttributeValue("2021-11-04T16:37:30Z"),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));
        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("2345"),
                "NhsNumber", new AttributeValue("9000000009"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), "somewhere")),
                "Content-Type", new AttributeValue("application/pdf"),
                "DocumentUploaded", new AttributeValue().withBOOL(false),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));


        aws.addDocument(aws.getDocumentStoreBucketName(), S3_KEY, S3_VALUE);
        aws.addDocument(aws.getDocumentStoreBucketName(), S3_KEY2, S3_VALUE2);
    }

    @Test
    void shouldGetDocumentsByNhsNumberandZipthem() throws IOException, InterruptedException {
        var searchRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentManifest?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9000000009"))
                .GET()
                .header("Authorization", createBearerToken())
                .build();
        var searchResponse = newHttpClient().send(searchRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(searchResponse.statusCode()).isEqualTo(200);
        assertThat(searchResponse.body()).contains(aws.getDocumentStoreBucketName()+"/patient-record-");
    }

    private String createBearerToken() {
        String jwt = JWT.create()
                .withClaim("email", "")
                .sign(none());
        return "Bearer " + jwt;
    }
    public static String getAwsHost() {
        return resolveContainerHost("localstack");
    }

}
