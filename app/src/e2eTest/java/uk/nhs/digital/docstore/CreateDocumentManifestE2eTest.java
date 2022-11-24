package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.auth0.jwt.JWT;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;
import uk.nhs.digital.docstore.helpers.BaseUriHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static com.auth0.jwt.algorithms.Algorithm.none;
import static java.net.http.HttpClient.newHttpClient;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.resolveContainerHost;

public class CreateDocumentManifestE2eTest {
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";
    private static final String AWS_REGION = "eu-west-2";
    private static final int DEFAULT_PORT = 4566;
    private static final String CODE_VALUE = "185361000000102";

    private final Map<String, String> document1 = Map.of("description","uploaded document 1","s3Key","key1","content","content 1");
    private final Map<String, String> document2 = Map.of("description","uploaded document 2","s3Key","key2","content","content 2");
    private final Map<String, String> document3 = Map.of("description","uploaded document 3","s3Key","key3","content","content 3");

    private AwsS3Helper aws;
    private AmazonDynamoDB dynamoDbClient;

    @BeforeEach
    void setUp() {
        var baseUri = String.format(BASE_URI_TEMPLATE, getAwsHost(), DEFAULT_PORT);
        var awsEndpointConfiguration = new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION);

        aws = new AwsS3Helper(awsEndpointConfiguration);

        dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(awsEndpointConfiguration)
                .build();

        ScanResult scanResult = dynamoDbClient.scan("DocumentReferenceMetadata", List.of("ID"));
        scanResult.getItems()
                .forEach(item -> dynamoDbClient.deleteItem("DocumentReferenceMetadata", item));

        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("1234"),
                "NhsNumber", new AttributeValue("9000000009"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), document1.get("s3Key"))),
                "ContentType", new AttributeValue("text/plain"),
                "DocumentUploaded", new AttributeValue().withBOOL(true),
                "Description", new AttributeValue(document1.get("description")),
                "Created", new AttributeValue("2021-11-04T15:57:30Z"),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));
        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("6987"),
                "NhsNumber", new AttributeValue("9000000001"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), document2.get("s3Key"))),
                "ContentType", new AttributeValue("text/plain"),
                "DocumentUploaded", new AttributeValue().withBOOL(true),
                "Description", new AttributeValue(document2.get("description")),
                "Created", new AttributeValue("2021-11-04T16:37:30Z"),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));
        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("5687"),
                "NhsNumber", new AttributeValue("9000000009"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), document3.get("s3Key"))),
                "ContentType", new AttributeValue("text/plain"),
                "DocumentUploaded", new AttributeValue().withBOOL(true),
                "Description", new AttributeValue(document3.get("description")),
                "Created", new AttributeValue("2021-11-04T17:37:30Z"),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));
        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("2345"),
                "NhsNumber", new AttributeValue("9000000009"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), "somewhere")),
                "Content-Type", new AttributeValue("application/pdf"),
                "DocumentUploaded", new AttributeValue().withBOOL(false),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));


        aws.addDocument(aws.getDocumentStoreBucketName(), document1.get("s3Key"), document1.get("content"));
        aws.addDocument(aws.getDocumentStoreBucketName(), document2.get("s3Key"), document2.get("content"));
        aws.addDocument(aws.getDocumentStoreBucketName(),document3.get("s3Key"), document3.get("content"));
    }

    @Test
    void shouldGetDocumentsByNhsNumberAndUploadZipToS3AndSaveMetadata() throws IOException, InterruptedException {
        var nhsNumber = "9000000009";
        var searchRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentManifest?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C"+nhsNumber))
                .GET()
                .header("Authorization", createBearerToken())
                .build();
        var searchResponse = newHttpClient().send(searchRequest, HttpResponse.BodyHandlers.ofString());
        var responseUrl =  JsonPath.<String>read(searchResponse.body(), "$.result.url");

        assertThat(searchResponse.statusCode()).isEqualTo(200);
        assertThat(responseUrl).contains(nhsNumber);

        var preSignedUrl = responseUrl.replace(BaseUriHelper.PRESIGNED_URL_REFERENCE_HOST, getAwsHost());
        var documentRequest = HttpRequest.newBuilder(URI.create(preSignedUrl))
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        var documentResponse = newHttpClient().send(documentRequest, HttpResponse.BodyHandlers.ofByteArray());

        var fileNames = listZipEntryNames(new ByteArrayInputStream(documentResponse.body()));

        assertThat(fileNames.size()).isEqualTo(2);
        assertThat(fileNames.get(0)).isEqualTo(document1.get("description"));
        assertThat(fileNames.get(1)).isEqualTo(document3.get("description"));

        var s3Location = "s3://" + responseUrl.substring(responseUrl.indexOf('d'), responseUrl.indexOf('?'));
        var scan = dynamoDbClient.scan(checkZipTraceExists(s3Location));

        assertThat(scan.getItems().size()).isEqualTo(1);
    }

    private ScanRequest checkZipTraceExists(String location) {
        var expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put("#location", "Location");

        var expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":location", new AttributeValue(location));

        return new ScanRequest()
                .withTableName("DocumentZipTrace")
                .withFilterExpression("#location = :location")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);
    }

    public ArrayList<Object> listZipEntryNames(ByteArrayInputStream inputStream) throws IOException {
        var fileNameArray = new ArrayList<>();
        var zipInputStream = new ZipInputStream(inputStream);
        var zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null) {
            fileNameArray.add(zipEntry.getName());
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.closeEntry();

        return fileNameArray;
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
