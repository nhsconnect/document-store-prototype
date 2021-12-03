package uk.nhs.digital.docstore.testHarness;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.testHarness.helpers.AuthorizedRequestBuilderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Objects.requireNonNull;
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
    void returnsCreatedDocument() throws IOException, InterruptedException, URISyntaxException {
        String expectedDocumentReference = getContentFromResource("CreatedDocumentReference.json");
        String content = getContentFromResource("CreateDocumentReferenceRequest.json");

        var createDocumentReferenceRequest = AuthorizedRequestBuilderFactory.newPostBuilder("DocumentReference", content)
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

        URI documentUploadUri = extractDocumentUri(documentReference);
        var documentUploadRequest = HttpRequest.newBuilder(documentUploadUri)
                .PUT(BodyPublishers.ofString("hello"))
                .build();
        var documentUploadResponse = newHttpClient().send(documentUploadRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentUploadResponse.statusCode()).isEqualTo(200);

        HttpResponse<String> retrievedDocumentReferenceResponse = waitAtMost(30, TimeUnit.SECONDS)
                .pollDelay(2, TimeUnit.SECONDS)
                .pollInterval(3, TimeUnit.SECONDS)
                .until(() -> getDocumentResponse(id), documentIsFinal());

        assertThat(retrievedDocumentReferenceResponse.statusCode()).isEqualTo(200);
        String retrievedDocumentReferenceResponseBody = retrievedDocumentReferenceResponse.body();
        assertThatJson(retrievedDocumentReferenceResponseBody)
                .inPath("$.indexed")
                .asString()
                .satisfies(indexed -> {
                    var indexedAsInstant = Instant.parse(indexed);
                    assertThat(indexedAsInstant).isAfter(Instant.now().minus(30, SECONDS));
                });

        URI documentDownloadUri = extractDocumentUri(retrievedDocumentReferenceResponseBody);
        var documentRequest = HttpRequest.newBuilder(documentDownloadUri)
                .GET()
                .timeout(Duration.ofSeconds(2))
                .build();
        var documentResponse = newHttpClient().send(documentRequest, BodyHandlers.ofString(UTF_8));
        assertThat(documentResponse.body()).isEqualTo("hello");
    }

    private HttpResponse<String> getDocumentResponse(String id) throws URISyntaxException, IOException, InterruptedException {
        var retrieveDocumentReferenceRequest = AuthorizedRequestBuilderFactory.newGetBuilder("DocumentReference/" + id).build();

        return newHttpClient().send(retrieveDocumentReferenceRequest, BodyHandlers.ofString(UTF_8));
    }

    private Predicate<HttpResponse<String>> documentIsFinal() {
        return response -> response.statusCode() == 200
                && "final".equals(JsonPath.read(response.body(), "$.docStatus"));
    }

    private URI extractDocumentUri(String documentReference) throws URISyntaxException {
        return new URI(JsonPath.<String>read(documentReference, "$.content[0].attachment.url")
                .replace(INTERNAL_DOCKER_HOST, getHost()));
    }

    private String getContentFromResource(String resourcePath) {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream stream = requireNonNull(classLoader.getResourceAsStream(resourcePath))) {
            return new String(stream.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("unable to load resource '%s'", resourcePath), e);
        }
    }
}
