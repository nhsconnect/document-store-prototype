package uk.nhs.digital.docstore.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.nhs.digital.docstore.exceptions.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.exceptions.MissingSearchParametersException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedSubjectIdentifierSystemException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommonUtilsTest {

    private CommonUtils utils;

    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private static final String SUBJECT_ID_PARAM_NAME = "subject:identifier";

    @BeforeEach
    void setUp() {
        utils = new CommonUtils();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            NHS_NUMBER_SYSTEM_ID + "|1234567890",
            "1234567890",
    })
    void extractsNhsNumberFromParameters(String parameter) {
        var expectedNhsNumber = "1234567890";
        var actualNhsNumber = utils.getNhsNumberFrom(Map.of(SUBJECT_ID_PARAM_NAME, parameter));
        assertThat(expectedNhsNumber).isEqualTo(actualNhsNumber);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "urn:another-system|9000000009,urn:another-system",
            "|9000000009,''"
    })
    void raisesAnExceptionIfTheSubjectIdentifierSystemCannotBeUnderstood(String subjectIdentifier, String systemIdentifier) {
        assertThatThrownBy(
                () -> utils.getNhsNumberFrom(Map.of(SUBJECT_ID_PARAM_NAME, subjectIdentifier)))
                .isInstanceOf(UnrecognisedSubjectIdentifierSystemException.class)
                .hasMessageContaining(systemIdentifier);
    }

    @ParameterizedTest
    @ValueSource(strings = "unrecognised-parameter")
    @NullSource
    void raisesAnExceptionIfNoRecognisedSearchParametersAreUsed(String parameterName) {
        Map<String, String> parameters = new HashMap<>();
        if (parameterName != null) {
            parameters.put(parameterName, "value");
        }

        assertThatThrownBy(() -> utils.getNhsNumberFrom(parameters))
                .isInstanceOf(MissingSearchParametersException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://fhir.nhs.uk/Id/nhs-number|"})
    @EmptySource
    void raisesAnExceptionIfTheSubjectIdentifierIsInvalid(String subjectIdentifier) {
        assertThatThrownBy(
                () -> utils.getNhsNumberFrom(Map.of(SUBJECT_ID_PARAM_NAME, subjectIdentifier)))
                .isInstanceOf(InvalidSubjectIdentifierException.class)
                .hasMessageContaining(subjectIdentifier);
    }
}
