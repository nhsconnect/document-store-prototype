package uk.nhs.digital.docstore.patientdetails;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SimpleHttpClient {
    HttpResponse<String> get(String rootUri, String path) {
        String uri = rootUri + path;
        try {
            var confirmNHSNumberRequest = HttpRequest.newBuilder(URI.create(uri))
                    .GET()
                    .build();
            return newHttpClient().send(confirmNHSNumberRequest, HttpResponse.BodyHandlers.ofString(UTF_8));
        }
        catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}