package uk.nhs.digital.docstore;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class RetrieveDocumentReferenceE2eTest {

    @Test
    void returnsDocumentReferenceResource() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(BaseUriHelper.getBaseUri().resolve("DocumentReference/1234"))
                .GET()
                .build();

        var response = newHttpClient().send(request, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type")).contains("application/fhir+json");

        String content = getContentFromResource("DocumentReference.json");

        assertThatJson(response.body()).isEqualTo(content);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

}
