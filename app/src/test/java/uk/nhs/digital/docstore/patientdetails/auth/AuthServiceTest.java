package uk.nhs.digital.docstore.patientdetails.auth;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void getAccessTokenFetchesAnAccessTokenFromTheNhsApiOAuthProvider() throws MissingEnvironmentVariableException {
        var mockHttpClient = Mockito.mock(AuthServiceHttpClient.class);
        var mockPatientSearchConfig = Mockito.mock(PatientSearchConfig.class);
        var mockJwtBuilder = Mockito.mock(SignedJwtBuilder.class);

        var accessToken = new AccessToken("testtoken", "500", "bearer","some-date-time");
        var signedJwt = "jwt";
        var nhsOauthEndpoint = "nhs-oauth-endpoint";

        when(mockJwtBuilder.build()).thenReturn(signedJwt);
        when(mockPatientSearchConfig.nhsOauthEndpoint()).thenReturn(nhsOauthEndpoint);
        when(mockHttpClient.fetchAccessToken(signedJwt, nhsOauthEndpoint)).thenReturn(accessToken);

        AuthService authService = new AuthService(mockHttpClient, mockPatientSearchConfig, mockJwtBuilder);
        assertThat(authService.getAccessToken()).isEqualTo(accessToken.getAccessToken());
    }
}