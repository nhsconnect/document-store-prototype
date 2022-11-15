package uk.nhs.digital.docstore.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.exceptions.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.exceptions.MissingSearchParametersException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedSubjectIdentifierSystemException;
import uk.nhs.digital.docstore.utils.TestLogAppender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.DocumentMetadataBuilder.theMetadata;

@ExtendWith(MockitoExtension.class)
class DocumentMetadataSearchServiceTest {
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String SUBJECT_ID_PARAM_NAME = "subject:identifier";
    private static final String JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2V4YW1wbGUuYXV0aDAuY29tLyIsImF1ZCI6Imh0dHBzOi8vYXBpLmV4YW1wbGUuY29tL2NhbGFuZGFyL3YxLyIsInN1YiI6InVzcl8xMjMiLCJpYXQiOjE0NTg3ODU3OTYsImV4cCI6MTQ1ODg3MjE5Nn0.CA7eaHjIHz5NxeIJoFK9krqaeZrPLwmMmgI_XiQiIkQ";

    private DocumentMetadataSearchService searchService;

    @Mock
    private DocumentMetadataStore metadataStore;

    private Map<String, String> headers = Map.of("Authorization", "Bearer " + JWT);

    private static String asQualifiedIdentifier(String nhsNumber) {
        return String.format("%s|%s", NHS_NUMBER_SYSTEM_ID, nhsNumber);
    }

    @BeforeEach
    void setUp() {
        searchService = new DocumentMetadataSearchService(metadataStore);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            NHS_NUMBER_SYSTEM_ID + "|1234567890",
            "1234567890",
    })
    void supportsDifferentIdentifierSyntaxes(String identifier) {
        String nhsNumber = "1234567890";
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(true);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));

        List<DocumentMetadata> documents = searchService.findByNhsNumberFromParameters(
                Map.of(SUBJECT_ID_PARAM_NAME, identifier),
                headers);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            SUBJECT_ID_PARAM_NAME,
            "subject.identifier",
    })
    void supportsDifferentSearchSyntaxes(String parameterName) {
        String nhsNumber = "9000000009";
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(true);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));

        List<DocumentMetadata> documents = searchService.findByNhsNumberFromParameters(
                Map.of(parameterName, asQualifiedIdentifier(nhsNumber)),
                headers);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @Test
    void omitsPreSignedUrlIfDocumentIsNotAvailable() {
        String nhsNumber = "9000000009";
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(false);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));

        List<DocumentMetadata> documents = searchService.findByNhsNumberFromParameters(
                Map.of(SUBJECT_ID_PARAM_NAME, asQualifiedIdentifier(nhsNumber)),
                headers);

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(metadataTemplate.build());
    }

    @ParameterizedTest
    @ValueSource(strings = "unrecognised-parameter")
    @NullSource
    void raisesAnExceptionIfNoRecognisedSearchParametersAreUsed(String parameterName) {
        Map<String, String> parameters = new HashMap<>();
        if (parameterName != null) {
            parameters.put(parameterName, "value");
        }

        assertThatThrownBy(() -> searchService.findByNhsNumberFromParameters(parameters, headers))
                .isInstanceOf(MissingSearchParametersException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "urn:another-system|9000000009,urn:another-system",
            "|9000000009,''"
    })
    void raisesAnExceptionIfTheSubjectIdentifierSystemCannotBeUnderstood(String subjectIdentifier, String systemIdentifier) {
        assertThatThrownBy(
                () -> searchService.findByNhsNumberFromParameters(
                        Map.of(SUBJECT_ID_PARAM_NAME, subjectIdentifier),
                        headers))
                .isInstanceOf(UnrecognisedSubjectIdentifierSystemException.class)
                .hasMessageContaining(systemIdentifier);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://fhir.nhs.uk/Id/nhs-number|"})
    @EmptySource
    void raisesAnExceptionIfTheSubjectIdentifierIsInvalid(String subjectIdentifier) {
        assertThatThrownBy(
                () -> searchService.findByNhsNumberFromParameters(
                        Map.of(SUBJECT_ID_PARAM_NAME, subjectIdentifier),
                        headers))
                .isInstanceOf(InvalidSubjectIdentifierException.class)
                .hasMessageContaining(subjectIdentifier);
    }

    @Test
    void logsTheSearchActionObfuscatingPii() {
        var testLogAppender = TestLogAppender.addTestLogAppender();
        String nhsNumber = "1234567890";
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of());

        searchService.findByNhsNumberFromParameters(
                Map.of(SUBJECT_ID_PARAM_NAME, nhsNumber),
                headers);

        assertThat(testLogAppender.findLoggedEvent("documents with NHS number ending 7890")).isNotNull();
    }
}
