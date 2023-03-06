package uk.nhs.digital.docstore.authoriser;

public class TokenRequestCookieFactory extends SecureCookie {

    public TokenRequestCookieFactory(String fieldName, String fieldContents, Long maxAge) {
        super(fieldName, fieldContents, maxAge);
    }
}
