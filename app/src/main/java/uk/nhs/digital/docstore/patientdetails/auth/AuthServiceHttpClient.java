package uk.nhs.digital.docstore.patientdetails.auth;

public class AuthServiceHttpClient {
    public AccessToken fetchAccessToken(String signedJwt) {
        return new AccessToken("", "", "");
    }
}
