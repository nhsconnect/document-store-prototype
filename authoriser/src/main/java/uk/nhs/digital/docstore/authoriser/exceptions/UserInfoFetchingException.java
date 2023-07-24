package uk.nhs.digital.docstore.authoriser.exceptions;

public class UserInfoFetchingException extends LoginException {
    public UserInfoFetchingException(String message) {
        super(message);
    }
}
