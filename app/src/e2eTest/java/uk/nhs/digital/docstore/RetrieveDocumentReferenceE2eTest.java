package uk.nhs.digital.docstore;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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

public class RetrieveDocumentReferenceE2eTest {
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4566;

    @SuppressWarnings("HttpUrlsUsage")
    public static final String BASE_URI_TEMPLATE = "http://%s:%d";

    @BeforeEach
    void setUp() {
        var baseUri = String.format(BASE_URI_TEMPLATE, getHost(), DEFAULT_PORT);
        AmazonDynamoDB dynamodbClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION))
                .build();
        ScanResult scanResult = dynamodbClient.scan("DocumentReferenceMetadata", List.of("ID"));
        scanResult.getItems().forEach(item -> dynamodbClient.deleteItem("DocumentReferenceMetadata", item));

        dynamodbClient.putItem("DocumentReferenceMetadata", Map.of("ID", new AttributeValue("1234"), "NhsNumber", new AttributeValue("12345")));
    }

    @Test
    void returnsDocumentReferenceResource() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference/1234"))
                .GET()
                .build();

        var response = newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).contains("application/fhir+json");

        String content = getContentFromResource("DocumentReference.json");

        assertThatJson(response.body()).isEqualTo(content);
    }

    @Test
    void returnsErrorWhenNoMatchingDocumentIsFound() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference/does-not-exist"))
                .GET()
                .build();

        var response = newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(response.body()).isEqualTo("{\n" +
                "  \"resourceType\": \"OperationOutcome\",\n" +
                "  \"issue\": [{\n" +
                "    \"severity\": \"error\",\n" +
                "    \"code\": \"not-found\",\n" +
                "    \"details\": {\n" +
                "      \"coding\": [{\n" +
                "        \"system\": \"https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1\",\n" +
                "        \"code\": \"NO_RECORD_FOUND\",\n" +
                "        \"display\": \"No record found\"\n" +
                "      }]\n" +
                "    }\n" +
                "  }]\n" +
                "}");
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    private static String getHost() {
        String host = System.getenv("DS_TEST_HOST");
        return (host != null) ? host : DEFAULT_HOST;
    }
}
