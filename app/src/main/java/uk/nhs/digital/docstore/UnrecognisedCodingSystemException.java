package uk.nhs.digital.docstore;

public class UnrecognisedCodingSystemException extends RuntimeException {
    public UnrecognisedCodingSystemException(String codingSystem) {
        super(String.format("Unrecognised coding system: '%s'", codingSystem));
    }
}
