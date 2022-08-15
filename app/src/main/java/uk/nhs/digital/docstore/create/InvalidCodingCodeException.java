package uk.nhs.digital.docstore.create;

public class InvalidCodingCodeException extends RuntimeException {

    public InvalidCodingCodeException(String code) {
        super(String.format("Invalid code: '%s'", code));
    }
}
