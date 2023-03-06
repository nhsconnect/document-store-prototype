package uk.nhs.digital.docstore.authoriser;

public class SecureCookie {
    private static final String BASE_COOKIE = "SameSite=Strict; Secure; HttpOnly";

    private String contents;

    public SecureCookie() {}

    public SecureCookie(String fieldName, String fieldContents, Long maxAge) {
        contents = contents + fieldName + "=" + fieldContents + "; ";
    }

    public void addField(String fieldName, String fieldContents) {
        contents = contents + fieldName + "=" + fieldContents + "; ";
    }

    @Override
    public String toString() {
        return contents + BASE_COOKIE;
    }
}
