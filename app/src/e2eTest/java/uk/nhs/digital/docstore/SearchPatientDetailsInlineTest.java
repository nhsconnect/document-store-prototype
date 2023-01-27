package uk.nhs.digital.docstore;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.SearchPatientDetailsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.config.StubbedPatientSearchConfig;
import uk.nhs.digital.docstore.handlers.SearchPatientDetailsHandler;

@ExtendWith(MockitoExtension.class)
public class SearchPatientDetailsInlineTest {
  @Mock private Context context;
  @Mock private AuditPublisher auditPublisher;

  private SearchPatientDetailsHandler searchPatientDetailsHandler;
  private RequestEventBuilder requestBuilder;

  @BeforeEach
  void setUp() {
    searchPatientDetailsHandler =
        new SearchPatientDetailsHandler(
            new StubbedApiConfig("http://ui-url"),
            new StubbedPatientSearchConfig(),
            auditPublisher);
    requestBuilder = new RequestEventBuilder();
  }

  @Test
  void returnsSuccessResponseWhenPatientDetailsFound() throws IOException {
    var request =
        requestBuilder.addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9000000009").build();

    var responseEvent = searchPatientDetailsHandler.handleRequest(request, context);

    assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
    assertThatJson(responseEvent.getBody())
        .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
        .isEqualTo(getContentFromResource("search-patient-details/patient-details-response.json"));
  }

  @Test
  void returnsSuccessResponseWhenPatientDetailsFoundWithLimitedInformation() throws IOException {
    var request =
        requestBuilder.addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9000000025").build();

    var responseEvent = searchPatientDetailsHandler.handleRequest(request, context);

    assertThat(responseEvent.getStatusCode()).isEqualTo(200);
    assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
    assertThatJson(responseEvent.getBody())
        .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
        .isEqualTo(
            getContentFromResource(
                "search-patient-details/patient-details-response-for-missing-information.json"));
  }

  @Test
  void returnsMissingPatientResponseWhenPatientDetailsNotFound() throws IOException {
    var request =
        requestBuilder.addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9111231130").build();

    var responseEvent = searchPatientDetailsHandler.handleRequest(request, context);

    assertThat(responseEvent.getStatusCode()).isEqualTo(404);
    assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
    assertThatJson(responseEvent.getBody())
        .whenIgnoringPaths("$.meta")
        .isEqualTo(getContentFromResource("search-patient-details/missing-patient-response.json"));
  }

  @Test
  void returnsErrorResponseWhenAnUnrecognisedSubjectIdentifierSystemIsInputted()
      throws IOException {
    var request =
        requestBuilder
            .addQueryParameter("unrecognised-subject-identifier-system|9000000009")
            .build();

    var responseEvent = searchPatientDetailsHandler.handleRequest(request, context);

    assertThat(responseEvent.getStatusCode()).isEqualTo(400);
    assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
    assertThatJson(responseEvent.getBody())
        .isEqualTo(getContentFromResource("errors/unrecognised-subject-identifier-system.json"));
  }

  @Test
  void returnsErrorResponseWhenAnInvalidSubjectIdentifierIsInputted() throws IOException {
    var request = requestBuilder.addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|").build();

    var responseEvent = searchPatientDetailsHandler.handleRequest(request, context);

    assertThat(responseEvent.getStatusCode()).isEqualTo(400);
    assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
    assertThatJson(responseEvent.getBody())
        .isEqualTo(getContentFromResource("errors/invalid-subject-identifier.json"));
  }

  @Test
  void returnsErrorResponseWhenSearchParametersAreMissing() throws IOException {
    var parameterlessRequest = requestBuilder.build();

    var responseEvent = searchPatientDetailsHandler.handleRequest(parameterlessRequest, context);

    assertThat(responseEvent.getStatusCode()).isEqualTo(400);
    assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
    assertThatJson(responseEvent.getBody())
        .isEqualTo(getContentFromResource("errors/missing-search-parameters.json"));
  }

  @Test
  void sendsAuditMessageToSqsWhenCallingPds() throws JsonProcessingException {
    var request =
        requestBuilder.addQueryParameter("https://fhir.nhs.uk/Id/nhs-number|9000000009").build();

    searchPatientDetailsHandler.handleRequest(request, context);

    verify(auditPublisher).publish(any(SearchPatientDetailsAuditMessage.class));
  }

  private String getContentFromResource(String resourcePath) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(resourcePath).getFile());
    return new String(Files.readAllBytes(file.toPath()));
  }

  public static class RequestEventBuilder {
    private final HashMap<String, String> parameters = new HashMap<>();

    RequestEventBuilder addQueryParameter(String value) {
      parameters.put("subject:identifier", value);
      return this;
    }

    private APIGatewayProxyRequestEvent build() {
      return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
    }
  }
}
