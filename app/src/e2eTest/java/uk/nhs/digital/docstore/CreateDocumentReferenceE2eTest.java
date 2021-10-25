package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.util.List;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.BaseUriHelper.getBaseUri;

public class CreateDocumentReferenceE2eTest {
    private static final String BASE_URI_TEMPLATE = "http://%s:%d";
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4566;

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
        s3Client.listObjects(documentStoreBucketName)
                .getObjectSummaries()
                .forEach(s3Object -> s3Client.deleteObject(documentStoreBucketName, s3Object.getKey()));
    }

    @Test
    void returnsCreatedDocumentReference() throws IOException, InterruptedException {
        String expectedDocumentReference = getContentFromResource("CreatedDocumentReference.json");
        var documentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference"))
                .POST(BodyPublishers.ofString(getContentFromResource("CreateDocumentReferenceRequest.json")))
                .header("Content-Type", "application/fhir+json")
                .header("Accept", "application/fhir+json")
                .build();

        var documentReferenceResponse = newHttpClient().send(documentReferenceRequest, BodyHandlers.ofString(UTF_8));

        var documentReference = documentReferenceResponse.body();
        String id = JsonPath.read(documentReference, "$.id");
        assertThat(documentReferenceResponse.statusCode())
                .isEqualTo(201);
        assertThat(documentReferenceResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThat(documentReferenceResponse.headers().firstValue("Location"))
                .hasValueSatisfying(location -> assertThat(location).endsWith("/DocumentReference/"+id));
        assertThatJson(documentReference)
                .whenIgnoringPaths("$.id", "$.content[*].attachment.url")
                .isEqualTo(expectedDocumentReference);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
