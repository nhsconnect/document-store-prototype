package uk.nhs.digital.docstore.authoriser.exceptions;

public class AuthorisationException extends Exception {
    public AuthorisationException(Exception e) {
        super(e);
    }
}
