package uk.nhs.digital.docstore.search;

public class MissingSearchParametersException extends RuntimeException {
    public MissingSearchParametersException(String expectedParameter) {
        super(String.format("expected '%s' but was missing", expectedParameter));
    }
}
