package uk.nhs.digital.docstore.patientdetails.auth;

public class AccessToken {
    private final String accessToken;
    private final String expiresIn;
    private final String tokenType;

    public AccessToken(String accessToken, String expiresIn, String tokenType) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
    }


    public String getAccessToken() {
        return accessToken;
    }
}
