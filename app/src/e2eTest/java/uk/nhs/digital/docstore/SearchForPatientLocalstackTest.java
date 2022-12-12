package uk.nhs.digital.docstore;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

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


public class SearchForPatientLocalstackTest {

    @Test
    void returnsSuccessResponse() throws IOException, InterruptedException {
        var expectedPatientDetailsResponse = (System.getProperty("PDS_FHIR_IS_STUBBED").equals("true") ||
                System.getenv("PDS_FHIR_IS_STUBBED").equals("true"))
                ? "search-patient-details/patient-details-stubbed-response.json"
                : "search-patient-details/patient-details-response.json";

        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9000000009"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(200);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource(expectedPatientDetailsResponse));
    }

    @DisabledIfSystemProperty(named = "PDS_FHIR_IS_STUBBED", matches = "true")
    @DisabledIfEnvironmentVariable(named = "PDS_FHIR_IS_STUBBED", matches = "true")
    @Test
    void returnsSuccessResponseForPatientWithMissingInformation() throws IOException, InterruptedException {
        var expectedPatientDetailsResponse = "search-patient-details/patient-details-response-for-missing-information.json";

        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9000000025"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(200);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource(expectedPatientDetailsResponse));
    }

    @DisabledIfSystemProperty(named = "PDS_FHIR_IS_STUBBED", matches = "true")
    @DisabledIfEnvironmentVariable(named = "PDS_FHIR_IS_STUBBED", matches = "true")
    @Test
    void returnsMissingPatientResponseWhenPatientNotFound() throws IOException, InterruptedException {
        var expectedPatientDetailsResponse = getContentFromResource("search-patient-details/missing-patient-response.json");
        var patientDetailsRequest = HttpRequest.newBuilder(getBaseUri().resolve("PatientDetails?subject:identifier=https://fhir.nhs.uk/Id/nhs-number%7C9111231130"))
                .GET()
                .build();

        var patientDetailsResponse = newHttpClient().send(patientDetailsRequest, HttpResponse.BodyHandlers.ofString(UTF_8));

        assertThat(patientDetailsResponse.statusCode()).isEqualTo(404);
        assertThat(patientDetailsResponse.headers().firstValue("Content-Type")).contains("application/fhir+json");
        assertThatJson(patientDetailsResponse.body())
                .whenIgnoringPaths("$.meta")
                .isEqualTo(expectedPatientDetailsResponse);
    }

    @Test
    void returnsErrorResponseWhenAnUnrecognisedSubjectIdentifierSystemIsInput() throws IOException, InterruptedException {
        var expectedPatientDetailsErrorResponse = getContentFromResource("errors/unrecognised-subject-identifier-system.json");
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
        var expectedPatientDetailsErrorResponse = getContentFromResource("errors/invalid-subject-identifier.json");
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
        var expectedPatientDetailsErrorResponse = getContentFromResource("errors/missing-search-parameters.json");
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

