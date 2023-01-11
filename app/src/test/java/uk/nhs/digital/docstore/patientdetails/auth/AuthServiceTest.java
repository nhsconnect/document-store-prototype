package uk.nhs.digital.docstore.patientdetails.auth;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterType;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void getAccessTokenFetchesAnAccessTokenFromTheNhsApiOAuthProvider() throws MissingEnvironmentVariableException {
        var mockHttpClient = Mockito.mock(AuthServiceHttpClient.class);
        var mockPatientSearchConfig = Mockito.mock(PatientSearchConfig.class);
        var mockJwtBuilder = Mockito.mock(SignedJwtBuilder.class);
        var mockSsm = Mockito.mock(AWSSimpleSystemsManagement.class);

        var accessToken = new AccessToken("testtoken", "500", "bearer","some-date-time");
        var signedJwt = "jwt";
        var nhsOauthEndpoint = "nhs-oauth-endpoint";
        var parameterName = "parameter-name";
        var putParameterRequest = new PutParameterRequest();
        putParameterRequest.withName(parameterName);
        putParameterRequest.withType(ParameterType.SecureString);
        putParameterRequest.withOverwrite(true);

        when(mockJwtBuilder.build()).thenReturn(signedJwt);
        when(mockPatientSearchConfig.nhsOauthEndpoint()).thenReturn(nhsOauthEndpoint);
        when(mockHttpClient.fetchAccessToken(signedJwt, nhsOauthEndpoint)).thenReturn(accessToken);
        when(mockPatientSearchConfig.pdsFhirTokenName()).thenReturn(parameterName);

        AuthService authService = new AuthService(mockHttpClient, mockPatientSearchConfig, mockJwtBuilder, mockSsm);
        assertThat(authService.retrieveAccessToken()).isEqualTo(accessToken.getAccessToken());
        verify(mockSsm).putParameter(putParameterRequest);
    }

    @Test
    void retrieveAccessTokenFromParameterStore() {
        var mockHttpClient = Mockito.mock(AuthServiceHttpClient.class);
        var mockPatientSearchConfig = Mockito.mock(PatientSearchConfig.class);
        var mockJwtBuilder = Mockito.mock(SignedJwtBuilder.class);
        var mockSsm = Mockito.mock(AWSSimpleSystemsManagement.class);
        var accessToken = "token";

        when(mockSsm.getParameter(any())).thenReturn(new GetParameterResult().withParameter(new Parameter().withValue(accessToken)));

        AuthService authService = new AuthService(mockHttpClient, mockPatientSearchConfig, mockJwtBuilder, mockSsm);
        assertThat(authService.retrieveAccessToken()).isEqualTo(accessToken);
    }

}