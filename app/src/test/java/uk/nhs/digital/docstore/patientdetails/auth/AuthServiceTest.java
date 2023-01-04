package uk.nhs.digital.docstore.patientdetails.auth;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void getAccessTokenFetchesAnAccessTokenFromTheNhsApiOAuthProvider() {
        var mockHttpClient = Mockito.mock(AuthServiceHttpClient.class);
        var accessToken = new AccessToken("testtoken", "500", "bearer");

        when(mockHttpClient.fetchAccessToken()).thenReturn(accessToken);

        AuthService authService = new AuthService(mockHttpClient);
        assertThat(authService.getAccessToken()).isEqualTo(accessToken.getAccessToken());
    }
}