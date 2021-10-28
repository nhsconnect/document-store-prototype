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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.BaseUriHelper.getBaseUri;

public class DocumentReferenceSearchE2eTest {

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
        s3Client.putObject(documentStoreBucketName, S3_KEY, S3_VALUE);

        dynamoDbClient.putItem(
                "DocumentReferenceMetadata",
                Map.of(
                        "ID", new AttributeValue("1234"),
                        "NhsNumber", new AttributeValue("12345"),
                        "Location", new AttributeValue(String.format("s3://%s/%s", documentStoreBucketName, S3_KEY)),
                        "ContentType", new AttributeValue("text/plain"),
                        "DocumentUploaded", new AttributeValue().withBOOL(true)));
    }

    @Test
    void returnsEmptyBundleWhenNoMatchesFound() throws IOException, InterruptedException {
        String expectedEmptySearchResponse = getContentFromResource("EmptyNHSNumberSearchResponse.json");
        var searchRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7Cdoesnt-exist"))
                .GET()
                .build();

        var searchResponse = newHttpClient().send(searchRequest, HttpResponse.BodyHandlers.ofString(UTF_8));
        assertThat(searchResponse.statusCode()).isEqualTo(200);
        assertThat(searchResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(searchResponse.body()).isEqualTo(expectedEmptySearchResponse);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }


    // return empty list (should be empty)
    // return one match
    // return multiple matches (maybe check links???)
    // return no link for prelim objects
    // return error for wrong parameters
}
