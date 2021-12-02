package uk.nhs.digital.docstore.search;

public class InvalidSubjectIdentifierException extends RuntimeException {
    public InvalidSubjectIdentifierException(String systemIdentifier) {
        super(String.format("invalid system identifier: '%s'", systemIdentifier));
    }
}
