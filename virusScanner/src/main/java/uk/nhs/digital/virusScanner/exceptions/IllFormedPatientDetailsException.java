package uk.nhs.digital.virusScanner.exceptions;

public class IllFormedPatientDetailsException extends Exception {
    public IllFormedPatientDetailsException(String cause) {
        super(cause);
    }
}
