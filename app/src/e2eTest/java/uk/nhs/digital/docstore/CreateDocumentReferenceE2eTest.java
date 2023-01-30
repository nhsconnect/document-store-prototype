package uk.nhs.digital.docstore;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getAwsHost;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.jayway.jsonpath.JsonPath;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;

public class CreateDocumentReferenceE2eTest {
    @SuppressWarnings("HttpUrlsUsage")
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";

    private static final String AWS_REGION = "eu-west-2";
    private static final int DEFAULT_PORT = 4566;
    private static final String INTERNAL_DOCKER_HOST = "localstack";

    @BeforeEach
    void setUp() {
        var baseUri = String.format(BASE_URI_TEMPLATE, getAwsHost(), DEFAULT_PORT);
        var awsEndpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION);

        AmazonDynamoDB dynamoDbClient =
                AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(awsEndpointConfiguration)
                        .build();
        ScanResult scanResult = dynamoDbClient.scan("DocumentReferenceMetadata", List.of("ID"));
        scanResult
                .getItems()
                .forEach(item -> dynamoDbClient.deleteItem("DocumentReferenceMetadata", item));

        var aws = new AwsS3Helper(awsEndpointConfiguration);
        var bucketName = aws.getDocumentStoreBucketName();
        aws.emptyBucket(bucketName);
    }

    @Test
    void returnsCreatedDocumentReference() throws IOException, InterruptedException {
        String expectedDocumentReference =
                getContentFromResource("create/created-document-reference.json");
        var requestContent =
                getContentFromResource("create/create-document-reference-request.json");
        var createDocumentReferenceRequest =
                HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference"))
                        .POST(BodyPublishers.ofString(requestContent))
                        .header("Content-Type", "application/fhir+json")
                        .header("Accept", "application/fhir+json")
                        .build();

        var createdDocumentReferenceResponse = getResponseFor(createDocumentReferenceRequest);
        var documentReference = createdDocumentReferenceResponse.body();

        assertThat(createdDocumentReferenceResponse.statusCode()).isEqualTo(201);
        String id = JsonPath.read(documentReference, "$.id");
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Location"))
                .hasValue("DocumentReference/" + id);
        assertThatJson(documentReference)
                .whenIgnoringPaths("$.id", "$.content[*].attachment.url", "$.meta")
                .isEqualTo(expectedDocumentReference);

        String documentUploadURL =
                JsonPath.<String>read(documentReference, "$.content[0].attachment.url")
                        .replace(INTERNAL_DOCKER_HOST, getAwsHost());
        URI documentUploadUri = URI.create(documentUploadURL);
        System.out.println("documentUploadUri: " + documentUploadUri);
        var documentUploadRequest =
                HttpRequest.newBuilder(documentUploadUri)
                        .PUT(BodyPublishers.ofString("hello"))
                        .build();
        var documentUploadResponse = getResponseFor(documentUploadRequest);
        assertThat(documentUploadResponse.statusCode()).isEqualTo(200);
    }

    @Test
    void returnsBadRequestIfCodingSystemIsNotSupported() throws IOException, InterruptedException {
        String expectedErrorResponse =
                getContentFromResource("create/unsupported-coding-system-response.json");
        var requestContent =
                getContentFromResource("create/unsupported-coding-system-request.json");
        var createRequest =
                HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference"))
                        .POST(BodyPublishers.ofString(requestContent))
                        .header("Content-Type", "application/fhir+json")
                        .header("Accept", "application/fhir+json")
                        .build();

        var errorResponse = getResponseFor(createRequest);

        assertThat(errorResponse.statusCode()).isEqualTo(400);
        assertThat(errorResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThatJson(errorResponse.body()).isEqualTo(expectedErrorResponse);
    }

    private HttpResponse<String> getResponseFor(HttpRequest request)
            throws IOException, InterruptedException {
        return newHttpClient().send(request, BodyHandlers.ofString(UTF_8));
    }

    private Predicate<HttpResponse<String>> documentIsFinal() {
        return response ->
                response.statusCode() == 200
                        && "final".equals(JsonPath.read(response.body(), "$.docStatus"));
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
