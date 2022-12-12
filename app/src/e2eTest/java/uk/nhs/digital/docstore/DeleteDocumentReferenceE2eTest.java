package uk.nhs.digital.docstore;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newHttpClient;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

public class DeleteDocumentReferenceE2eTest {

    @Test
    void shouldReturnResponseStatusAsSuccessful() throws IOException, InterruptedException  {
        var deleteDocumentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference"))
                .DELETE()
                .build();

        var deleteDocumentReferenceResponse = newHttpClient().send(deleteDocumentReferenceRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(deleteDocumentReferenceResponse.statusCode()).isEqualTo("200");
    }
}
