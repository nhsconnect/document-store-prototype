package uk.nhs.digital.docstore.patientdetails.auth;

public class AuthService {
    private final AuthServiceHttpClient httpClient;
    private final SignedJwtBuilder jwtBuilder;

    public AuthService(AuthServiceHttpClient httpClient, SignedJwtBuilder jwtBuilder) {
        this.httpClient = httpClient;
        this.jwtBuilder = jwtBuilder;
    }

    public String getAccessToken() {
        var signedJwt = jwtBuilder.build();
        var accessTokenResponse = httpClient.fetchAccessToken(signedJwt);
        return accessTokenResponse.getAccessToken();
    }
}