package uk.nhs.digital.docstore;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class HelloWorldE2eTest {

    @Test
    void returnsHelloWorldResponse() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(BaseUriHelper.getBaseUri().resolve("hello"))
                .GET()
                .build();

        var response = newHttpClient().send(request, BodyHandlers.ofString(UTF_8));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("Hello World!");
    }
}
