package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.create.InvalidCodingCodeException;
import uk.nhs.digital.docstore.create.MissingRequiredValueException;
import uk.nhs.digital.docstore.search.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.search.MissingSearchParametersException;
import uk.nhs.digital.docstore.search.UnrecognisedSubjectIdentifierSystemException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseGeneratorTest {
    private static final FhirContext fhirContext = FhirContext.forR4();
    private ErrorResponseGenerator errorResponseGenerator;
    private IParser jsonParser;

    @BeforeEach
    void setUp() {
        this.errorResponseGenerator = new ErrorResponseGenerator();
        jsonParser = fhirContext.newJsonParser();
    }

    @Test
    void returnsBadRequestIfSearchParametersAreInvalid() throws IOException {
        String expectedErrorResponse = getContentFromResource("search/unrecognised-subject-identifier.json");
        var response = errorResponseGenerator.errorResponse(new UnrecognisedSubjectIdentifierSystemException("system"), jsonParser);

        assertThat(response.getStatusCode())
                .isEqualTo(400);
        assertResponseHasExpectedHeaders(response);
        assertThatJson(response.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfSystemIdentifierValueIsMissing() throws IOException {
        String expectedErrorResponse = getContentFromResource("search/invalid-search-response.json");
        var response = errorResponseGenerator.errorResponse(new InvalidSubjectIdentifierException("identifier"), jsonParser);

        assertThat(response.getStatusCode())
                .isEqualTo(400);
        assertResponseHasExpectedHeaders(response);
        assertThatJson(response.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfSearchParametersAreMissing() throws IOException {
        String expectedErrorResponse = getContentFromResource("search/missing-search-parameters-response.json");

        var response = errorResponseGenerator.errorResponse(new MissingSearchParametersException("subject:identifier"), jsonParser);

        assertThat(response.getStatusCode())
                .isEqualTo(400);
        assertResponseHasExpectedHeaders(response);
        assertThatJson(response.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfDocumentTypeCodingSystemIsInvalid() throws IOException {
        String expectedErrorResponse = getContentFromResource("create/unsupported-coding-system-response.json");

        var response = errorResponseGenerator.errorResponse(new UnrecognisedCodingSystemException("https://invalid-coding-system"), jsonParser);

        assertThat(response.getStatusCode())
                .isEqualTo(400);
        assertResponseHasExpectedHeaders(response);
        assertThatJson(response.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfDocumentDescriptionIsMissing() throws IOException {
        String expectedErrorResponse = getContentFromResource("create/missing-document-description-response.json");

        var response = errorResponseGenerator.errorResponse(new MissingRequiredValueException("document-description"), jsonParser);

        assertThat(response.getStatusCode())
                .isEqualTo(400);
        assertResponseHasExpectedHeaders(response);
        assertThatJson(response.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfCodingCodeIsInvalid() throws IOException {
        String expectedErrorResponse = getContentFromResource("create/invalid-coding-code-response.json");

        var response = errorResponseGenerator.errorResponse(new InvalidCodingCodeException("invalid-code"), jsonParser);

        assertThat(response.getStatusCode())
                .isEqualTo(400);
        assertResponseHasExpectedHeaders(response);
        assertThatJson(response.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnInternalServerErrorForOtherErrors() throws IOException {
        String expectedErrorResponse = getContentFromResource("search/internal-server-error-response.json");

        var response = errorResponseGenerator.errorResponse(new Exception(), jsonParser);

        assertThat(response.getStatusCode())
                .isEqualTo(500);
        assertResponseHasExpectedHeaders(response);
        assertThatJson(response.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    private void assertResponseHasExpectedHeaders(APIGatewayProxyResponseEvent searchResponse) {
        assertThat(searchResponse.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        assertThat(searchResponse.getHeaders().get("Access-Control-Allow-Origin"))
                .isEqualTo("http://localhost:4566");
        assertThat(searchResponse.getHeaders().get("Access-Control-Allow-Methods"))
                .isEqualTo("GET, OPTIONS, POST");
    }
}
