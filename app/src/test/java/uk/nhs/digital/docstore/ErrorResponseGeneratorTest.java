package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.config.StubbedApiConfig;
import uk.nhs.digital.docstore.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseGeneratorTest {
    private static final FhirContext fhirContext = FhirContext.forR4();
    public static final String AMPLIFY_BASE_URL = "http://deployed-url-for-cors-origin";
    private ErrorResponseGenerator errorResponseGenerator;
    private IParser jsonParser;

    @BeforeEach
    void setUp() {
        this.errorResponseGenerator = new ErrorResponseGenerator(new StubbedApiConfig(AMPLIFY_BASE_URL));
        jsonParser = fhirContext.newJsonParser();
    }

    @Test
    void returnsBadRequestForExceptionsThatSerializeToOperationOutcomeIssues() throws IOException {
        String expectedErrorResponse = getContentFromResource("test-exception-response.json");

        TestException exception = new TestException();
        var response = errorResponseGenerator.errorResponse(exception, jsonParser);
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

        var response = errorResponseGenerator.errorResponse(new Exception(), jsonParser);
        var headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(500);
        assertThat(headers.get("Content-Type")).isEqualTo("application/fhir+json");
        assertThat(headers.get("Access-Control-Allow-Origin")).isEqualTo(AMPLIFY_BASE_URL);
        assertThat(headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, OPTIONS, POST");
        assertThatJson(response.getBody()).isEqualTo(expectedErrorResponse);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

}
