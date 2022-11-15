package uk.nhs.digital.docstore;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.resolveContainerHost;
import static uk.nhs.digital.docstore.json.JsonMapper.toJson;


public class SearchForPatientLocalstackTest {

    @BeforeAll
    static public void pointWiremockToPdsAdaptorContainer() {
        WireMock.configureFor(resolveContainerHost("pds-adaptor"), 8080);
    }

    @Test
    void returnsSuccessResponse() throws IOException, InterruptedException {
        Map<String, Object> patientData = Map.of("nhsNumber", "9000000009",
                "givenName", List.of("Jane"),
                "familyName", "Doe",
                "postalCode", "LS1 6AE",
                "birthdate", "1998-07-11");

        stubFor(get(urlEqualTo("/patient-trace-information/9000000009"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(toJson(patientData))));

        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9000000009"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(200);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource("search-patient-details/patient-details-response.json"));
    }

    @Test
    @Disabled("Will fail while pds adaptor is stubbed as there is no patient not found case")
    void returnsMissingPatientResponseWhenPatientNotFound() throws IOException, InterruptedException {
        String expectedPatientDetailsResponse = getContentFromResource("search-patient-details/missing-patient-response.json");
        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9111231130"))
                .GET()
                .build();

        stubFor(get(urlEqualTo("/patient-trace-information/9111231130"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(200);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .whenIgnoringPaths("$.meta")
                .isEqualTo(expectedPatientDetailsResponse);
    }

    @Test
    void returnsErrorResponseWhenAnUnrecognisedSubjectIdentifierSystemIsInput() throws IOException, InterruptedException {
        String expectedPatientDetailsErrorResponse = getContentFromResource("errors/unrecognised-subject-identifier-system.json");
        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=unrecognised-subject-identifier-system%7C9000000009"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(400);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .isEqualTo(expectedPatientDetailsErrorResponse);
    }

    @Test
    void returnsErrorResponseWhenAnInvalidSubjectIdentifierIsInput() throws IOException, InterruptedException {
        String expectedPatientDetailsErrorResponse = getContentFromResource("errors/invalid-subject-identifier.json");
        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7Cinvalid-subject-identifier"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(400);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .isEqualTo(expectedPatientDetailsErrorResponse);
    }

    @Test
    void returnsErrorResponseWhenSearchParametersAreMissing() throws IOException, InterruptedException {
        String expectedPatientDetailsErrorResponse = getContentFromResource("errors/missing-search-parameters.json");
        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(400);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .isEqualTo(expectedPatientDetailsErrorResponse);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}

