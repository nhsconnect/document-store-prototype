package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
import uk.nhs.digital.docstore.patientdetails.PdsFhirClient;
import uk.nhs.digital.docstore.patientdetails.SearchPatientDetailsHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.json.JsonMapper.toJson;

@WireMockTest(httpPort = SearchForPatientInlineTest.LocalhostPdsAdaptorNoStubbingPatientSearchConfig.PDS_ADAPTOR_PORT)
@ExtendWith(MockitoExtension.class)
public class SearchForPatientInlineTest {

    @Mock
    private Context context;
    private SearchPatientDetailsHandler handler;
    private RequestEventBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        var patientSearchConfig = new LocalhostPdsAdaptorNoStubbingPatientSearchConfig();
        handler = new SearchPatientDetailsHandler(new StubbedApiConfig("http://ui-url"), new PdsFhirClient(patientSearchConfig));
        requestBuilder = new RequestEventBuilder();
    }

    @Test
    void returnsUsableResponseWhenDefaultToStubbedResponses() {
        var defaultConfigWithStubbingOn = new PatientSearchConfig();
        handler = new SearchPatientDetailsHandler(new StubbedApiConfig("http://ui-url"), new PdsFhirClient(defaultConfigWithStubbingOn));

        var reasonableRequest = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000009")
                .build();

        var responseEvent = handler.handleRequest(reasonableRequest, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    }

    @Test
    void returnsSuccessResponse() throws IOException, InterruptedException {
       var patientData = getContentFromResource("search-patient-details/pds-fhir-responses/complete-patient-details-response.json");
        stubFor(get(urlEqualTo("/Patient/9000000009"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(patientData)));

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
    void returnsMissingPatientResponseWhenPatientNotFound() throws IOException, InterruptedException {

        stubFor(get(urlEqualTo("/patient-trace-information/9111231130"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9111231130")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
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
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number%7Cinvalid-subject-identifier")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/invalid-subject-identifier.json"));
    }

    @Test
    void returnsErrorResponseWhenSearchParametersAreMissing() throws IOException, InterruptedException {
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

    public static class LocalhostPdsAdaptorNoStubbingPatientSearchConfig extends PatientSearchConfig {
        public static final int PDS_ADAPTOR_PORT = 8081;

        @Override
        public String pdsAdaptorRootUri() {
            return String.format("http://localhost:%d/", PDS_ADAPTOR_PORT);
        }

        @Override
        public boolean pdsAdaptorIsStubbed() {
            return false;
        }
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

