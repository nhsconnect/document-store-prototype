package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Predicate;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

public class CreateDocumentReferenceE2eTest {
    @SuppressWarnings("HttpUrlsUsage")
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4566;
    private static final String INTERNAL_DOCKER_HOST = "172.17.0.2";

    private String documentStoreBucketName;
    private AmazonS3 s3Client;

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

        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(awsEndpointConfiguration)
                .enablePathStyleAccess()
                .build();
        var terraformOutput = getContentFromResource("terraform.json");
        documentStoreBucketName = JsonPath.read(terraformOutput, "$.document-store-bucket.value");
        s3Client.listObjects(documentStoreBucketName)
                .getObjectSummaries()
                .forEach(s3Object -> s3Client.deleteObject(documentStoreBucketName, s3Object.getKey()));
    }

    @Test
    void returnsCreatedDocumentReference() throws IOException, InterruptedException {
        String expectedDocumentReference = getContentFromResource("create/CreatedDocumentReference.json");
        var createDocumentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference"))
                .POST(BodyPublishers.ofString(getContentFromResource("create/CreateDocumentReferenceRequest.json")))
                .header("Content-Type", "application/fhir+json")
                .header("Accept", "application/fhir+json")
                .build();

        var createdDocumentReferenceResponse = getResponseFor(createDocumentReferenceRequest);

        var documentReference = createdDocumentReferenceResponse.body();
        String id = JsonPath.read(documentReference, "$.id");
        assertThat(createdDocumentReferenceResponse.statusCode())
                .isEqualTo(201);
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Location"))
                .hasValue("DocumentReference/" + id);
        assertThatJson(documentReference)
                .whenIgnoringPaths("$.id", "$.content[*].attachment.url", "$.meta")
                .isEqualTo(expectedDocumentReference);

        String documentUploadURL = JsonPath.<String>read(documentReference, "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost());
        URI documentUploadUri = URI.create(documentUploadURL);
        var documentUploadRequest = HttpRequest.newBuilder(documentUploadUri)
                .PUT(BodyPublishers.ofString("hello"))
                .build();
        var documentUploadResponse = getResponseFor(documentUploadRequest);
        assertThat(documentUploadResponse.statusCode()).isEqualTo(200);

        var retrieveDocumentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference/" + id))
                .GET()
                .build();
        HttpResponse<String> retrievedDocumentReferenceResponse = waitAtMost(ofSeconds(15))
                .pollDelay(ofMillis(500))
                .pollInterval(ofSeconds(1))
                .until(() -> getResponseFor(retrieveDocumentReferenceRequest), documentIsFinal());
        assertThatJson(retrievedDocumentReferenceResponse.body())
                .inPath("$.indexed")
                .isString();

        String preSignedUrl = JsonPath.<String>read(retrievedDocumentReferenceResponse.body(), "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost());
        var documentRequest = HttpRequest.newBuilder(URI.create(preSignedUrl))
                .GET()
                .timeout(ofSeconds(2))
                .build();
        var documentResponse = getResponseFor(documentRequest);
        assertThat(documentResponse.body()).isEqualTo("hello");
    }

    @Test
    void returnsBadRequestIfCodingSystemIsNotSupported() throws IOException, InterruptedException {
        String expectedErrorResponse = getContentFromResource("create/unsupported-coding-system-response.json");
        var createRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference"))
                .POST(BodyPublishers.ofString(getContentFromResource("create/unsupported-coding-system-request.json")))
                .header("Content-Type", "application/fhir+json")
                .header("Accept", "application/fhir+json")
                .build();

        var errorResponse = getResponseFor(createRequest);

        assertThat(errorResponse.statusCode())
                .isEqualTo(400);
        assertThat(errorResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThatJson(errorResponse.body())
                .isEqualTo(expectedErrorResponse);
    }

    private HttpResponse<String> getResponseFor(HttpRequest request) throws IOException, InterruptedException {
        return newHttpClient().send(request, BodyHandlers.ofString(UTF_8));
    }

    private Predicate<HttpResponse<String>> documentIsFinal() {
        return response -> response.statusCode() == 200
                && "final".equals(JsonPath.read(response.body(), "$.docStatus"));
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
