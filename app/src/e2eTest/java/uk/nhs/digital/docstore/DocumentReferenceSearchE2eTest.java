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

import java.io.File;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.auth0.jwt.algorithms.Algorithm.none;
import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.*;

public class DocumentReferenceSearchE2eTest {
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";
    private static final String AWS_REGION = "eu-west-2";
    private static final int DEFAULT_PORT = 4566;
    private static final String S3_KEY = "abcd";
    private static final String S3_VALUE = "content";
    private static final String CODE_VALUE = "185361000000102";


    @BeforeEach
    void setUp() throws IOException {
        var baseUri = String.format(BASE_URI_TEMPLATE, getAwsHost(), DEFAULT_PORT);
        var awsEndpointConfiguration = new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION);

        AwsS3Helper aws = new AwsS3Helper(awsEndpointConfiguration);

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
                "ID", new AttributeValue("2345"),
                "NhsNumber", new AttributeValue("9000000009"),
                "Location", new AttributeValue(String.format("s3://%s/%s", aws.getDocumentStoreBucketName(), "somewhere")),
                "Content-Type", new AttributeValue("application/pdf"),
                "DocumentUploaded", new AttributeValue().withBOOL(false),
                "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE))));


        aws.addDocument(aws.getDocumentStoreBucketName(), S3_KEY, S3_VALUE);
    }

    @Test
    void returnsEmptyBundleWhenNoMatchesFound() throws IOException, InterruptedException {
        String expectedEmptySearchResponse = getContentFromResource("search/empty-nhs-number-response.json");
        var searchRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9111231130"))
                .GET()
                .header("x-localstack-authorization", createBearerToken())
                .build();

        var searchResponse = newHttpClient().send(searchRequest, BodyHandlers.ofString(UTF_8));

        assertThat(searchResponse.statusCode()).isEqualTo(200);
        assertThat(searchResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(searchResponse.body())
                .whenIgnoringPaths("$.meta")
                .isEqualTo(expectedEmptySearchResponse);
    }

    @Test
    void returnsMatchingResults() throws IOException, InterruptedException {
        String expectedSearchResponse = getContentFromResource("search/nhs-number-response.json");
        var searchRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9000000009"))
                .GET()
                .header("x-localstack-authorization", createBearerToken())
                .build();

        var searchResponse = newHttpClient().send(searchRequest, BodyHandlers.ofString(UTF_8));

        assertThat(searchResponse.statusCode()).isEqualTo(200);
        assertThat(searchResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(searchResponse.body())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta", "$.entry[*].resource.indexed")
                .isEqualTo(expectedSearchResponse);
        assertThatJson(searchResponse.body())
                .inPath("$.entry[0].resource.indexed")
                .isString();
    }

    @Test
    void returnsBadRequestIfSearchParametersAreInvalid() throws IOException, InterruptedException {
        String expectedErrorResponse = getContentFromResource("errors/unrecognised-subject-identifier-system.json");
        var searchRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference?subject.identifier=unknown-system%7C9000000009"))
                .GET()
                .header("x-localstack-authorization", createBearerToken())
                .build();

        var searchResponse = newHttpClient().send(searchRequest, BodyHandlers.ofString(UTF_8));

        assertThat(searchResponse.statusCode())
                .isEqualTo(400);
        assertThat(searchResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThatJson(searchResponse.body())
                .isEqualTo(expectedErrorResponse);
    }

    private String createBearerToken() {
        String jwt = JWT.create()
                .withClaim("email", "")
                .sign(none());
        return "Bearer " + jwt;
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}