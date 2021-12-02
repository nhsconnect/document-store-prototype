package uk.nhs.digital.docstore.search;

public class UnrecognisedSubjectIdentifierSystemException extends RuntimeException {
    public UnrecognisedSubjectIdentifierSystemException(String systemIdentifier) {
        super(String.format("unrecognised system: '%s'", systemIdentifier));
    }
}
