package uk.nhs.digital.docstore;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.nhs.digital.docstore.DocumentMetadataBuilder.theMetadata;

@ExtendWith(MockitoExtension.class)
class DocumentReferenceSearchServiceTest {
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String SUBJECT_ID_PARAM_NAME = "subject:identifier";
    private static final URL NO_PRE_SIGNED_URL = null;

    private DocumentReferenceSearchService searchService;

    @Mock
    private DocumentMetadataStore metadataStore;
    @Mock
    private DocumentStore documentStore;

    private static String asQualifiedIdentifier(String nhsNumber) {
        return String.format("%s|%s", NHS_NUMBER_SYSTEM_ID, nhsNumber);
    }

    @BeforeEach
    void setUp() {
        searchService = new DocumentReferenceSearchService(metadataStore, documentStore);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            SUBJECT_ID_PARAM_NAME,
            "subject.identifier",
    })
    void supportsDifferentSearchSyntaxes(String parameterName) throws MalformedURLException {
        String nhsNumber = randomNumeric(11);
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(true);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));
        when(documentStore.generatePreSignedUrl(any()))
                .thenReturn(new URL("https://example.org/"));

        List<Document> documents = searchService.findByParameters(
                Map.of(parameterName, asQualifiedIdentifier(nhsNumber)));

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new Document(metadataTemplate.build(),
                        new URL("https://example.org/")));
    }

    @Test
    void omitsPreSignedUrlIfDocumentIsNotAvailable() {
        String nhsNumber = randomNumeric(11);
        var metadataTemplate = theMetadata()
                .withNhsNumber(nhsNumber)
                .withDocumentUploaded(false);
        when(metadataStore.findByNhsNumber(nhsNumber))
                .thenReturn(List.of(metadataTemplate.build()));

        List<Document> documents = searchService.findByParameters(
                Map.of(SUBJECT_ID_PARAM_NAME, asQualifiedIdentifier(nhsNumber)));

        assertThat(documents)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new Document(
                        metadataTemplate.build(),
                        NO_PRE_SIGNED_URL));
        verify(documentStore, never())
                .generatePreSignedUrl(any());
    }

    @ParameterizedTest
    @ValueSource(strings = "unrecognised-parameter")
    @NullSource
    void raisesAnExceptionIfNoRecognisedSearchParametersAreUsed(String parameterName) {
        Map<String, String> parameters = new HashMap<>();
        if (parameterName != null) {
            parameters.put(parameterName, "value");
        }

        assertThatThrownBy(() -> searchService.findByParameters(parameters))
                .isInstanceOf(MissingSearchParametersException.class);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "urn:another-system|12345,urn:another-system",
            "|12345,''"
    })
    void raisesAnExceptionIfTheSubjectIdentifierSystemCannotBeUnderstood(String subjectIdentifier, String systemIdentifier) {
        assertThatThrownBy(
                () -> searchService.findByParameters(
                        Map.of(SUBJECT_ID_PARAM_NAME, subjectIdentifier)))
                .isInstanceOf(UnrecognisedSubjectIdentifierSystemException.class)
                .hasMessageContaining(systemIdentifier);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://fhir.nhs.uk/Id/nhs-number|"})
    @EmptySource
    void raisesAnExceptionIfTheSubjectIdentifierIsInvalid(String subjectIdentifier) {
        assertThatThrownBy(
                () -> searchService.findByParameters(
                        Map.of(SUBJECT_ID_PARAM_NAME, subjectIdentifier)))
                .isInstanceOf(InvalidSubjectIdentifierException.class)
                .hasMessageContaining(subjectIdentifier);
    }
}
