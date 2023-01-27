package uk.nhs.digital.docstore.exceptions;

public class MissingEnvironmentVariableException extends Exception {
  public MissingEnvironmentVariableException(String message) {
    super(message);
  }
}
