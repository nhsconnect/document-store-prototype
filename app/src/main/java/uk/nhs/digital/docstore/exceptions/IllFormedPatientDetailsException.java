package uk.nhs.digital.docstore.exceptions;

public class IllFormedPatientDetailsException extends Exception {
    public IllFormedPatientDetailsException(String cause) {
        super(cause);
    }
}
