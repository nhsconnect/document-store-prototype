package uk.nhs.digital.docstore.authoriser.exceptions;

public class LoginException extends Exception {

    public LoginException(String message) {
        super(message);
    }

    public LoginException(Exception e) {
        super(e);
    }
}
