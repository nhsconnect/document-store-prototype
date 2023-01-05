package uk.nhs.digital.docstore.config;

public class MissingEnvironmentVariableException extends Exception {
    public MissingEnvironmentVariableException(String message) {
        super(message);
    }
}
