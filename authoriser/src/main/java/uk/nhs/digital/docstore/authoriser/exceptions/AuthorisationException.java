package uk.nhs.digital.docstore.authoriser.exceptions;

public class AuthorisationException extends LoginException {

    public AuthorisationException(Exception e) {
        super(e);
    }
}
