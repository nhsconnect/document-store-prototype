package uk.nhs.digital.docstore.patientdetails.auth;

import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

public class AuthService {
    private final AuthServiceHttpClient httpClient;
    private final PatientSearchConfig patientSearchConfig;
    private final SignedJwtBuilder jwtBuilder;

    public AuthService(AuthServiceHttpClient httpClient, PatientSearchConfig patientSearchConfig) {
        this(httpClient, patientSearchConfig, new SignedJwtBuilder(patientSearchConfig));
    }

    public AuthService(AuthServiceHttpClient httpClient, PatientSearchConfig patientSearchConfig, SignedJwtBuilder jwtBuilder) {
        this.httpClient = httpClient;
        this.patientSearchConfig = patientSearchConfig;
        this.jwtBuilder = jwtBuilder;
    }

    public String getAccessToken() {
        var signedJwt = jwtBuilder.build();
        var accessTokenResponse = httpClient.fetchAccessToken(signedJwt, patientSearchConfig.nhsOauthEndpoint());
        return accessTokenResponse.getAccessToken();
    }
}