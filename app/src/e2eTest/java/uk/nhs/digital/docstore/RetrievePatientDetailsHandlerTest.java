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
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

public class RetrievePatientDetailsHandlerTest {
    @Test
    void returnsSuccessResponse() throws IOException, InterruptedException {
        String expectedPatientDetailsResponse = getContentFromResource("retrieve-patient-details/patient-details-response.json");
        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9000000009"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(200);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.content[*].attachment.url", "$.entry[*].resource.meta", "$.entry[*].resource.indexed")
                .isEqualTo(expectedPatientDetailsResponse);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}

