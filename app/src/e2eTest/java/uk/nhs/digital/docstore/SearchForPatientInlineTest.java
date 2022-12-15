package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.config.StubbedPatientSearchConfig;
import uk.nhs.digital.docstore.patientdetails.SearchPatientDetailsHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SearchForPatientInlineTest {

    @Mock
    private Context context;
    private SearchPatientDetailsHandler handler;
    private RequestEventBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        handler = new SearchPatientDetailsHandler(new StubbedApiConfig("http://ui-url"), new StubbedPatientSearchConfig());
        requestBuilder = new RequestEventBuilder();
    }

    @Test
    void returnsSuccessResponse() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000009")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource("search-patient-details/patient-details-response.json"));
    }

    @Test
    void returnsSuccessResponseWithLimitedInformation() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000025")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource("search-patient-details/patient-details-response-for-missing-information.json"));
    }

    @Test
    void returnsMissingPatientResponseWhenPatientNotFound() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9111231130")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(404);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta")
                .isEqualTo(getContentFromResource("search-patient-details/missing-patient-response.json"));
    }

    @Test
    void returnsErrorResponseWhenAnUnrecognisedSubjectIdentifierSystemIsInput() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "unrecognised-subject-identifier-system|9000000009")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/unrecognised-subject-identifier-system.json"));
    }

    @Test
    void returnsErrorResponseWhenAnInvalidSubjectIdentifierIsInput() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/invalid-subject-identifier.json"));
    }

    @Test
    void returnsErrorResponseWhenSearchParametersAreMissing() throws IOException {
        var parameterlessRequest = requestBuilder.build();

        var responseEvent = handler.handleRequest(parameterlessRequest, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/missing-search-parameters.json"));
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static class RequestEventBuilder {
        private HashMap<String, String> parameters = new HashMap<>();

        private RequestEventBuilder addQueryParameter(String name, String value) {
            parameters.put(name, value);
            return this;
        }

        private APIGatewayProxyRequestEvent build() {
            return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
        }
    }
}

