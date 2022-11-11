package uk.nhs.digital.docstore.search;

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
import uk.nhs.digital.docstore.common.DocumentMetadataSearchService;
import uk.nhs.digital.docstore.exceptions.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.exceptions.MissingSearchParametersException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedSubjectIdentifierSystemException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.DocumentMetadataBuilder.theMetadata;

@ExtendWith(MockitoExtension.class)
class DocumentMetadataSearchServiceTest {
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String SUBJECT_ID_PARAM_NAME = "subject:identifier";

    private DocumentMetadataSearchService searchService;

    @Mock
    private DocumentMetadataStore metadataStore;
    @Mock
    private Consumer<String> logger;

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
                logger);

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
                logger);

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
                logger);

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

        assertThatThrownBy(() -> searchService.findByNhsNumberFromParameters(parameters, logger))
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
                        logger))
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
                        logger))
                .isInstanceOf(InvalidSubjectIdentifierException.class)
                .hasMessageContaining(subjectIdentifier);
    }

    @Test
    void logsTheSearchActionObfuscatingPii() {
        String nhsNumber = "1234567890";
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of());

        searchService.findByNhsNumberFromParameters(
                Map.of(SUBJECT_ID_PARAM_NAME, nhsNumber),
                logger);

        verify(logger).accept("documents with NHS number ending 7890");
    }
}
