package uk.nhs.digital.docstore;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

public class RetrievePatientDetailsHandlerTest {
    @Test
    void returnsSuccessResponse() throws IOException, InterruptedException {
        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C12345"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(200);
    }
}
