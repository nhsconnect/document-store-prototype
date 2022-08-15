package uk.nhs.digital.docstore.create;

public class MissingRequiredValueException extends RuntimeException {

    public MissingRequiredValueException(String field) {
        super(String.format("Missing required field: '%s'", field));
    }
}
