package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
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

@WireMockTest(httpPort = SearchForPatientInlineTest.StubbedPatientSearchConfig.PDS_ADAPTOR_PORT)
@ExtendWith(MockitoExtension.class)
public class SearchForPatientInlineTest {

    @Mock
    private Context context;
    private SearchPatientDetailsHandler handler;
    private RequestEventBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        handler = new SearchPatientDetailsHandler(new StubbedPatientSearchConfig());
        requestBuilder = new RequestEventBuilder();
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

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static class StubbedPatientSearchConfig extends PatientSearchConfig {
        public static final int PDS_ADAPTOR_PORT = 8081;

        @Override
        public String pdsAdaptorRootUri() {
            return String.format("http://localhost:%d/", PDS_ADAPTOR_PORT);
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

