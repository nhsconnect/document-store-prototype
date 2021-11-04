package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

public class RetrieveDocumentReferenceE2eTest {
    @SuppressWarnings("HttpUrlsUsage")
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4566;
    private static final String S3_KEY = "abcd";
    private static final String S3_VALUE = "content";
    private static final String INTERNAL_DOCKER_HOST = "172.17.0.2";

    private static String getHost() {
        String host = System.getenv("DS_TEST_HOST");
        return (host != null) ? host : DEFAULT_HOST;
    }

    @BeforeEach
    void setUp() throws IOException {
        var baseUri = String.format(BASE_URI_TEMPLATE, getHost(), DEFAULT_PORT);
        var awsEndpointConfiguration = new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION);

        AmazonDynamoDB dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(awsEndpointConfiguration)
                .build();
        ScanResult scanResult = dynamoDbClient.scan("DocumentReferenceMetadata", List.of("ID"));
        scanResult.getItems()
                .forEach(item -> dynamoDbClient.deleteItem("DocumentReferenceMetadata", item));

        var s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(awsEndpointConfiguration)
                .enablePathStyleAccess()
                .build();
        var terraformOutput = getContentFromResource("terraform.json");
        String documentStoreBucketName = JsonPath.read(terraformOutput, "$.document-store-bucket.value");

        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("1234"),
                "NhsNumber", new AttributeValue("12345"),
                "Location", new AttributeValue(String.format("s3://%s/%s", documentStoreBucketName, S3_KEY)),
                "ContentType", new AttributeValue("text/plain"),
                "DocumentUploaded", new AttributeValue().withBOOL(true),
                "Description", new AttributeValue("uploaded document"),
                "Created", new AttributeValue("2021-11-04T15:57:30Z")));
        dynamoDbClient.putItem("DocumentReferenceMetadata", Map.of(
                "ID", new AttributeValue("3456"),
                "NhsNumber", new AttributeValue("56789"),
                "Location", new AttributeValue(String.format("s3://%s/%s", documentStoreBucketName, S3_KEY)),
                "ContentType", new AttributeValue("image/jpeg"),
                "DocumentUploaded", new AttributeValue().withBOOL(false)));

        s3Client.putObject(documentStoreBucketName, S3_KEY, S3_VALUE);
    }

    @Test
    void returnsDocumentReferenceResource() throws IOException, InterruptedException {
        String expectedDocumentReference = getContentFromResource("retrieve/document-reference.json");
        var documentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference/1234"))
                .GET()
                .build();

        var documentReferenceResponse = newHttpClient().send(documentReferenceRequest, BodyHandlers.ofString(UTF_8));

        var documentReference = documentReferenceResponse.body();
        assertThat(documentReferenceResponse.statusCode()).isEqualTo(200);
        assertThat(documentReferenceResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(documentReference)
                .whenIgnoringPaths("$.content[*].attachment.url", "$.meta", "$.indexed")
                .isEqualTo(expectedDocumentReference);
        assertThatJson(documentReference)
                .inPath("$.indexed")
                .isString();

        String preSignedUrl = JsonPath.<String>read(documentReference, "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost());
        var documentRequest = HttpRequest.newBuilder(URI.create(preSignedUrl))
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        var documentResponse = newHttpClient().send(documentRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentResponse.body()).isEqualTo(S3_VALUE);
    }

    @Test
    void excludesContentPropertiesIfTheDocumentHasNotBeenUploaded() throws IOException, InterruptedException {
        String expectedDocumentReference = getContentFromResource("retrieve/preliminary-document-reference.json");
        var documentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference/3456"))
                .GET()
                .build();

        var documentReferenceResponse = newHttpClient().send(documentReferenceRequest, BodyHandlers.ofString(UTF_8));

        var documentReference = documentReferenceResponse.body();
        assertThat(documentReferenceResponse.statusCode())
                .isEqualTo(200);
        assertThat(documentReferenceResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThatJson(documentReference)
                .whenIgnoringPaths("$.meta")
                .isEqualTo(expectedDocumentReference);
    }

    @Test
    void returnsErrorWhenNoMatchingDocumentIsFound() throws IOException, InterruptedException {
        String expectedOutcome = getContentFromResource("retrieve/not-found.json");
        var request = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference/does-not-exist"))
                .GET()
                .build();

        var response = newHttpClient().send(request, BodyHandlers.ofString(UTF_8));

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(response.body())
                .whenIgnoringPaths("$.meta")
                .isEqualTo(expectedOutcome);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
