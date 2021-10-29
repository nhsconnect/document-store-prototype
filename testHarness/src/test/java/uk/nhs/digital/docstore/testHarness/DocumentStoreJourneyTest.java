package uk.nhs.digital.docstore.testHarness;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.testHarness.helpers.AuthorizedRequestBuilderFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.time.Duration;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentStoreJourneyTest {
    private static final String DEFAULT_HOST = "localhost";
    private static final String INTERNAL_DOCKER_HOST = "172.17.0.2";

    private static String getHost() {
        String host = System.getenv("DS_TEST_HOST");
        return (host != null) ? host : DEFAULT_HOST;
    }

    @Test
    void returnsCreatedDocumentReference() throws IOException, InterruptedException, URISyntaxException {
        String docStoreUrl = System.getenv("DOCUMENT_STORE_BASE_URI");
        System.out.println("Doc store url:" + docStoreUrl);
        URI  apiGatewayEndpoint = URI.create(docStoreUrl);

        String expectedDocumentReference = getContentFromResource("CreatedDocumentReference.json");
        String content = getContentFromResource("CreateDocumentReferenceRequest.json");

        var createDocumentReferenceRequest = AuthorizedRequestBuilderFactory.newBuilder(apiGatewayEndpoint, "DocumentReference", content)
                .POST(BodyPublishers.ofString(content))
                .header("Content-Type", "application/fhir+json")
                .header("Accept", "application/fhir+json")
                .build();

        var createdDocumentReferenceResponse = newHttpClient().send(createDocumentReferenceRequest, BodyHandlers.ofString(UTF_8));

        var documentReference = createdDocumentReferenceResponse.body();
        String id = JsonPath.read(documentReference, "$.id");
        assertThat(createdDocumentReferenceResponse.statusCode())
                .isEqualTo(201);
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Content-Type"))
                .contains("application/fhir+json");
        assertThat(createdDocumentReferenceResponse.headers().firstValue("Location"))
                .hasValue("DocumentReference/" + id);
        assertThatJson(documentReference)
                .whenIgnoringPaths("$.id", "$.content[*].attachment.url")
                .isEqualTo(expectedDocumentReference);

        String documentUploadURL = JsonPath.<String>read(documentReference, "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost());
        URI documentUploadUri = URI.create(documentUploadURL);
        var documentUploadRequest = HttpRequest.newBuilder(documentUploadUri)
                .PUT(BodyPublishers.ofString("hello"))
                .build();
        var documentUploadResponse = newHttpClient().send(documentUploadRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentUploadResponse.statusCode()).isEqualTo(200);

        var retrieveDocumentReferenceRequest = AuthorizedRequestBuilderFactory.newBuilder(apiGatewayEndpoint,"DocumentReference/" + id, null )
                .GET()
                .build();

        var retrievedDocumentReferenceResponse = newHttpClient().send(retrieveDocumentReferenceRequest, BodyHandlers.ofString(UTF_8));
        assertThat(retrievedDocumentReferenceResponse.statusCode()).isEqualTo(200);
        assertThatJson(retrievedDocumentReferenceResponse.body())
                .inPath("$.docStatus")
                .isEqualTo("final");

        String preSignedUrl = JsonPath.<String>read(retrievedDocumentReferenceResponse.body(), "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost());
        var documentRequest = HttpRequest.newBuilder(URI.create(preSignedUrl))
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        var documentResponse = newHttpClient().send(documentRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentResponse.body()).isEqualTo("hello");
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
