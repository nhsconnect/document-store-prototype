package uk.nhs.digital.docstore.authoriser.exceptions;

public class TokenFetchingException extends LoginException {
    public TokenFetchingException(String message) {
        super(message);
    }
}
