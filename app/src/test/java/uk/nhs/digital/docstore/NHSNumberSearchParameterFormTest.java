package uk.nhs.digital.docstore;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;
import uk.nhs.digital.docstore.exceptions.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.exceptions.MissingSearchParametersException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedSubjectIdentifierSystemException;
import uk.nhs.digital.docstore.model.NhsNumber;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NHSNumberSearchParameterFormTest {

    @Test
    void validateDoesNotThrowExceptionWhenSearchParameterIsPresentAndValid() {
        Map<String, String> searchParameters = Map.of("subject:identifier","https://fhir.nhs.uk/Id/nhs-number|9000000009");
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(searchParameters);

        assertThatNoException().isThrownBy(() -> nhsNumberSearchParameterForm.validate());
    }

    @Test
    void constructorThrowsMissingSearchParametersExceptionWhenSubjectIdentifierParameterIsMissing() {
        assertThatThrownBy(() -> new NHSNumberSearchParameterForm(Map.of()))
                .isExactlyInstanceOf(MissingSearchParametersException.class);
    }

    @Test
    void validateThrowsInvalidSubjectIdentifierExceptionWhenAnInvalidSubjectIdentifierIsInput() {
        Map<String, String> searchParameters =  Map.of("subject:identifier","https://fhir.nhs.uk/Id/nhs-number|90000");
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(searchParameters);

        assertThatThrownBy(() -> nhsNumberSearchParameterForm.validate())
                .isExactlyInstanceOf(InvalidSubjectIdentifierException.class);

    }

    @Test
    void validateThrowsInvalidSubjectIdentifierExceptionWhenAnInvalidSubjectIdentifierIsBlankInput() {
        Map<String, String> searchParameters =  Map.of("subject:identifier","https://fhir.nhs.uk/Id/nhs-number|");
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(searchParameters);

        assertThatThrownBy(() -> nhsNumberSearchParameterForm.validate())
                .isExactlyInstanceOf(InvalidSubjectIdentifierException.class);

    }

    @Test
    void validateThrowsUnrecognisedSubjectIdentifierSystemExceptionWhenAnUnrecognisedSubjectIdentifierSystemIsInput() {
        Map<String, String> searchParameters =  Map.of("subject:identifier","unrecognised-subject-identifier-system|9000000009");
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(searchParameters);

        assertThatThrownBy(() -> nhsNumberSearchParameterForm.validate())
                .isExactlyInstanceOf(UnrecognisedSubjectIdentifierSystemException.class);
    }

    @Test
    void returnNhsNumberReturnsNhsNumberIfSubjectIdentifierIsValid() throws IllFormedPatentDetailsException {
        Map<String, String> searchParameters =  Map.of("subject:identifier","https://fhir.nhs.uk/Id/nhs-number|9000000009");
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(searchParameters);

        assertTrue(nhsNumberSearchParameterForm.getNhsNumber().equals(new NhsNumber("9000000009")));
    }
}
