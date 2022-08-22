package uk.nhs.digital.docstore;

import uk.nhs.digital.docstore.exceptions.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.exceptions.MissingSearchParametersException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedSubjectIdentifierSystemException;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NHSNumberSearchParameterForm {
    private static final Pattern SUBJECT_IDENTIFIER_PATTERN = Pattern.compile("^(?<systempart>(?<system>.*?)(?<!\\\\)\\|)?(?<identifier>.*)$");
    private static final Pattern NHS_NUMBER_ID_PATTERN = Pattern.compile("\\d{10}");
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private boolean isValid = false;
    private final Matcher matcher;

    public NHSNumberSearchParameterForm(Map<String, String> searchParameters) {
        String subject = Optional.ofNullable(searchParameters.get("subject:identifier"))
                .or(() -> Optional.ofNullable(searchParameters.get("subject.identifier")))
                .orElseThrow(() -> new MissingSearchParametersException("subject:identifier"));
        this.matcher = SUBJECT_IDENTIFIER_PATTERN.matcher(subject);
    }

    public void validate() {
        if (!matcher.matches() || matcher.group("identifier").isBlank() || !(NHS_NUMBER_ID_PATTERN.matcher(matcher.group("identifier")).matches())) {
            throw new InvalidSubjectIdentifierException(matcher.group("identifier"));
        }
        if (matcher.group("systempart") != null && !NHS_NUMBER_SYSTEM_ID.equals(matcher.group("system"))) {
            throw new UnrecognisedSubjectIdentifierSystemException(matcher.group("system"));
        }
        isValid = true;
    }

    public String getNhsNumber() {
        if (!isValid){
            validate();
        }
        return matcher.group("identifier");
    }
}
