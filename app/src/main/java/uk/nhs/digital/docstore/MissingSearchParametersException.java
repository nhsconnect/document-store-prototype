package uk.nhs.digital.docstore;

public class MissingSearchParametersException extends RuntimeException {
    public MissingSearchParametersException(String expectedParameter) {
        super(String.format("expected '%s' but was missing", expectedParameter));
    }
}
