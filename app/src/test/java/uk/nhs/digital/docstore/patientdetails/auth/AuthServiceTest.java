package uk.nhs.digital.docstore.patientdetails.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
import uk.nhs.digital.docstore.utils.SSMService;

class AuthServiceTest {

    @Test
    void getAccessTokenFetchesAnAccessTokenFromTheNhsApiOAuthProvider()
            throws MissingEnvironmentVariableException {
        var mockHttpClient = Mockito.mock(AuthServiceHttpClient.class);
        var mockPatientSearchConfig = Mockito.mock(PatientSearchConfig.class);
        var mockJwtBuilder = Mockito.mock(SignedJwtBuilder.class);
        var mockSsmService = Mockito.mock(SSMService.class);
        var mockSsm = Mockito.mock(AWSSimpleSystemsManagement.class);

        var accessToken = new AccessToken("testtoken", "500", "bearer", "some-date-time");
        var signedJwt = "jwt";
        var nhsOauthEndpoint = "nhs-oauth-endpoint";
        var parameterName = "parameter-name";
        var putParameterRequest = new PutParameterRequest();
        putParameterRequest.withName(parameterName);
        putParameterRequest.withValue(accessToken.getAccessToken());
        putParameterRequest.withOverwrite(true);

        when(mockJwtBuilder.build()).thenReturn(signedJwt);
        when(mockPatientSearchConfig.nhsOauthEndpoint()).thenReturn(nhsOauthEndpoint);
        when(mockHttpClient.fetchAccessToken(signedJwt, nhsOauthEndpoint)).thenReturn(accessToken);
        when(mockPatientSearchConfig.pdsFhirTokenName()).thenReturn(parameterName);
        when(mockSsmService.getClient()).thenReturn(mockSsm);

        AuthService authService =
                new AuthService(
                        mockHttpClient, mockPatientSearchConfig, mockJwtBuilder, mockSsmService);
        assertThat(authService.getNewAccessToken()).isEqualTo(accessToken.getAccessToken());
        verify(mockSsm).putParameter(putParameterRequest);
    }

    @Test
    void retrieveAccessToken() {
        var mockHttpClient = Mockito.mock(AuthServiceHttpClient.class);
        var mockPatientSearchConfig = Mockito.mock(PatientSearchConfig.class);
        var mockJwtBuilder = Mockito.mock(SignedJwtBuilder.class);
        var mockSsmService = Mockito.mock(SSMService.class);
        var accessToken = "token";

        when(mockSsmService.retrieveParameterStoreValue(any())).thenReturn(accessToken);

        AuthService authService =
                new AuthService(
                        mockHttpClient, mockPatientSearchConfig, mockJwtBuilder, mockSsmService);
        assertThat(authService.retrieveAccessToken()).isEqualTo(accessToken);
    }
}
