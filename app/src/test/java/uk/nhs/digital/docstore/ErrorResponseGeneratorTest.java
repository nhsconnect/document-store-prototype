package uk.nhs.digital.docstore;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import ca.uhn.fhir.context.FhirContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.exceptions.TestException;

public class ErrorResponseGeneratorTest {
  private static final FhirContext fhirContext = FhirContext.forR4();
  public static final String AMPLIFY_BASE_URL = "http://deployed-url-for-cors-origin";
  private ErrorResponseGenerator errorResponseGenerator;

  @BeforeEach
  void setUp() {
    this.errorResponseGenerator =
        new ErrorResponseGenerator(new StubbedApiConfig(AMPLIFY_BASE_URL));
  }

  @Test
  void returnsBadRequestForExceptionsThatSerializeToOperationOutcomeIssues() throws IOException {
    String expectedErrorResponse = getContentFromResource("test-exception-response.json");

    TestException exception = new TestException("Invalid coding code");
    var response = errorResponseGenerator.errorResponse(exception);
    var headers = response.getHeaders();

    assertThat(response.getStatusCode()).isEqualTo(400);
    assertThat(headers.get("Content-Type")).isEqualTo("application/fhir+json");
    assertThat(headers.get("Access-Control-Allow-Origin")).isEqualTo(AMPLIFY_BASE_URL);
    assertThat(headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, OPTIONS, POST");
    assertThatJson(response.getBody()).isEqualTo(expectedErrorResponse);
  }

  @Test
  void returnInternalServerErrorForOtherErrors() throws IOException {
    String expectedErrorResponse = getContentFromResource("internal-server-error-response.json");

    var response = errorResponseGenerator.errorResponse(new Exception());
    var headers = response.getHeaders();

    assertThat(response.getStatusCode()).isEqualTo(500);
    assertThat(headers.get("Content-Type")).isEqualTo("application/fhir+json");
    assertThat(headers.get("Access-Control-Allow-Origin")).isEqualTo(AMPLIFY_BASE_URL);
    assertThat(headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, OPTIONS, POST");
    assertThatJson(response.getBody()).isEqualTo(expectedErrorResponse);
  }

  @Test
  void return507ForOutOfMemoryErrors() {
    var error = "out of memory error";

    var response = errorResponseGenerator.outOfMemoryResponse(new OutOfMemoryError(error));
    var headers = response.getHeaders();

    assertThat(response.getStatusCode()).isEqualTo(507);
    assertThat(headers.get("Content-Type")).isEqualTo("application/fhir+json");
    assertThat(headers.get("Access-Control-Allow-Origin")).isEqualTo(AMPLIFY_BASE_URL);
    assertThat(headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, OPTIONS, POST");
    assertThat(response.getBody()).isEqualTo("File too large: " + error);
  }

  private String getContentFromResource(String resourcePath) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(resourcePath).getFile());
    return new String(Files.readAllBytes(file.toPath()));
  }
}
