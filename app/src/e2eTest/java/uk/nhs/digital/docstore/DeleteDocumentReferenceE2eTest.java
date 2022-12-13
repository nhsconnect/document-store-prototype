package uk.nhs.digital.docstore;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static java.net.http.HttpClient.newHttpClient;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

public class DeleteDocumentReferenceE2eTest {

    @Test
    void shouldReturnTheNhsNumberParameter() throws IOException, InterruptedException  {
        var nhsNumber = "1234567890";
        var nhsNumberParameter = URLEncoder.encode("https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber, StandardCharsets.UTF_8);
        var deleteDocumentReferenceRequest = HttpRequest.newBuilder(getBaseUri().resolve("DocumentReference?subject:identifier=" + nhsNumberParameter))
                .DELETE()
                .build();

        var deleteDocumentReferenceResponse = newHttpClient().send(deleteDocumentReferenceRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(deleteDocumentReferenceResponse.statusCode()).isEqualTo(200);
        assertThat(deleteDocumentReferenceResponse.body()).isEqualTo(nhsNumber);
    }
}
