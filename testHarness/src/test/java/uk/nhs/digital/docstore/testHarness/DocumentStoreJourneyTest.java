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
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

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

        String expectedDocumentReference = getContentFromResource("CreatedDocumentReference.json");
        String content = getContentFromResource("CreateDocumentReferenceRequest.json");

        var createDocumentReferenceRequest = AuthorizedRequestBuilderFactory.newPostBuilder(docStoreUrl, "DocumentReference", content)
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
                .whenIgnoringPaths("$.id", "$.content[*].attachment.url", "$.meta")
                .isEqualTo(expectedDocumentReference);

        String documentUploadURL = JsonPath.<String>read(documentReference, "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost());
        URI documentUploadUri = URI.create(documentUploadURL);
        var documentUploadRequest = HttpRequest.newBuilder(documentUploadUri)
                .PUT(BodyPublishers.ofString("hello"))
                .build();
        var documentUploadResponse = newHttpClient().send(documentUploadRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentUploadResponse.statusCode()).isEqualTo(200);

        waitAtMost(30, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(documentIsFinal(docStoreUrl, id));

        var retrievedDocumentReferenceResponse = getDocumentResponse(docStoreUrl, id);
        assertThat(retrievedDocumentReferenceResponse.statusCode()).isEqualTo(200);
        assertThatJson(retrievedDocumentReferenceResponse.body())
                .inPath("$.indexed")
                .asString()
                .satisfies(indexed -> {
                    var indexedAsInstant = Instant.parse(indexed);
                    assertThat(indexedAsInstant).isAfter(Instant.now().minus(30, SECONDS));
                });

        String preSignedUrl = JsonPath.<String>read(retrievedDocumentReferenceResponse.body(), "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost());
        var documentRequest = HttpRequest.newBuilder(URI.create(preSignedUrl))
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        var documentResponse = newHttpClient().send(documentRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentResponse.body()).isEqualTo("hello");
    }

    private Callable<Boolean> documentIsFinal(String docStoreUrl, String id) {
        return () -> {
            HttpResponse<String> retrievedDocumentReferenceResponse = getDocumentResponse(docStoreUrl, id);
            return JsonPath.read(retrievedDocumentReferenceResponse.body(), "$.docStatus").equals("final");
        };
    }

    private HttpResponse<String> getDocumentResponse(String docStoreUrl, String id) throws URISyntaxException, IOException, InterruptedException {
        var retrieveDocumentReferenceRequest = AuthorizedRequestBuilderFactory.newGetBuilder(docStoreUrl, "DocumentReference/" + id).build();

        return newHttpClient().send(retrieveDocumentReferenceRequest, BodyHandlers.ofString(UTF_8));
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
