package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        var searchResponse = errorResponseGenerator.errorResponse(new UnrecognisedSubjectIdentifierSystemException("system"), jsonParser);

        assertThat(searchResponse.getStatusCode())
                .isEqualTo(400);
        assertThat(searchResponse.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        assertThatJson(searchResponse.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfSystemIdentifierValueIsMissing() throws IOException {
        String expectedErrorResponse = getContentFromResource("search/invalid-search-response.json");
        var searchResponse = errorResponseGenerator.errorResponse(new InvalidSubjectIdentifierException("identifier"), jsonParser);

        assertThat(searchResponse.getStatusCode())
                .isEqualTo(400);
        assertThat(searchResponse.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        assertThatJson(searchResponse.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfSearchParametersAreMissing() throws IOException {
        String expectedErrorResponse = getContentFromResource("search/missing-search-parameters-response.json");

        var searchResponse = errorResponseGenerator.errorResponse(new MissingSearchParametersException("subject:identifier"), jsonParser);

        assertThat(searchResponse.getStatusCode())
                .isEqualTo(400);
        assertThat(searchResponse.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        assertThatJson(searchResponse.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnsBadRequestIfDocumentTypeCodingSystemIsInvalid() throws IOException {
        String expectedErrorResponse = getContentFromResource("create/unsupported-coding-system-response.json");

        var createResponse = errorResponseGenerator.errorResponse(new UnrecognisedCodingSystemException("coding-system"), jsonParser);

        assertThat(createResponse.getStatusCode())
                .isEqualTo(400);
        assertThat(createResponse.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        assertThatJson(createResponse.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    @Test
    void returnInternalServerErrorForOtherErrors() throws IOException {
        String expectedErrorResponse = getContentFromResource("search/internal-server-error-response.json");

        var searchResponse = errorResponseGenerator.errorResponse(new Exception(), jsonParser);

        assertThat(searchResponse.getStatusCode())
                .isEqualTo(500);
        assertThat(searchResponse.getHeaders().get("Content-Type"))
                .isEqualTo("application/fhir+json");
        assertThatJson(searchResponse.getBody())
                .isEqualTo(expectedErrorResponse);
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
