package uk.nhs.digital.docstore.create;

public class DocumentReferenceValidationException extends RuntimeException {
    public DocumentReferenceValidationException(String field) {
        super(String.format("Invalid field: '%s'", field));
    }
}
