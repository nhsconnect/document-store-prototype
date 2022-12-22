package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.config.StubbedPatientSearchConfig;
import uk.nhs.digital.docstore.patientdetails.SearchPatientDetailsHandler;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
public class SearchPatientDetailsInlineTest {
    @Mock
    private Context context;
    @Mock
    private AmazonSQS amazonSqsClient;

    @Captor
    ArgumentCaptor<SendMessageRequest> messageRequestCaptor;

    @SystemStub
    private EnvironmentVariables environmentVariables;

    private SearchPatientDetailsHandler handler;
    private RequestEventBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        handler = new SearchPatientDetailsHandler(
                new StubbedApiConfig("http://ui-url"),
                new StubbedPatientSearchConfig(),
                new SplunkPublisher(amazonSqsClient)
        );
        requestBuilder = new RequestEventBuilder();
    }

    @Test
    void returnsSuccessResponseWhenPatientDetailsFound() throws IOException {
        var request = requestBuilder
                .addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9000000009")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource("search-patient-details/patient-details-response.json"));
    }

    @Test
    void returnsSuccessResponseWhenPatientDetailsFoundWithLimitedInformation() throws IOException {
        var request = requestBuilder
                .addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9000000025")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource("search-patient-details/patient-details-response-for-missing-information.json"));
    }

    @Test
    void returnsMissingPatientResponseWhenPatientDetailsNotFound() throws IOException {
        var request = requestBuilder
                .addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9111231130")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(404);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta")
                .isEqualTo(getContentFromResource("search-patient-details/missing-patient-response.json"));
    }

    @Test
    void returnsErrorResponseWhenAnUnrecognisedSubjectIdentifierSystemIsInputted() throws IOException {
        var request = requestBuilder
                .addQueryParameter("unrecognised-subject-identifier-system|9000000009")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        verify(amazonSqsClient, never()).sendMessage(any());
        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/unrecognised-subject-identifier-system.json"));
    }

    @Test
    void returnsErrorResponseWhenAnInvalidSubjectIdentifierIsInputted() throws IOException {
        var request = requestBuilder
                .addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        verify(amazonSqsClient, never()).sendMessage(any());
        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/invalid-subject-identifier.json"));
    }

    @Test
    void returnsErrorResponseWhenSearchParametersAreMissing() throws IOException {
        var parameterlessRequest = requestBuilder.build();

        var responseEvent = handler.handleRequest(parameterlessRequest, context);

        verify(amazonSqsClient, never()).sendMessage(any());
        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/missing-search-parameters.json"));
    }

    @Test
    void sendsAuditMessageToSqs() {
        var request = requestBuilder
                .addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9000000009")
                .build();
        var queueUrl = "document-store-audit-queue-url";
        var now = Instant.now();
        var expectedMessageBody = new JSONObject();
        expectedMessageBody.put("nhsNumber", "9000000009");
        expectedMessageBody.put("pdsResponseStatus", 200);
        expectedMessageBody.put("dateTime", now.toString());

        environmentVariables.set("SQS_QUEUE_URL", queueUrl);
        handler.handleRequest(request, context);

        verify(amazonSqsClient).sendMessage(messageRequestCaptor.capture());
        var messageBody = messageRequestCaptor.getValue().getMessageBody();
        assertThatJson(messageBody).whenIgnoringPaths("dateTime").isEqualTo(expectedMessageBody);
        var dateTime = JsonPath.read(messageBody, "$.dateTime").toString();
        assertThat(Instant.parse(dateTime)).isCloseTo(now, within(1, ChronoUnit.SECONDS));
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static class RequestEventBuilder {
        private final HashMap<String, String> parameters = new HashMap<>();

        private RequestEventBuilder addQueryParameter(String value) {
            parameters.put("subject:identifier", value);
            return this;
        }

        private APIGatewayProxyRequestEvent build() {
            return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
        }
    }
}
