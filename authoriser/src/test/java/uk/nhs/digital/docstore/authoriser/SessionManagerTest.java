package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;

class SessionManagerTest {

    @Test
    public void throwsErrorIfTokenCannotBeExchanged() throws Exception {
        var oidcClient = Mockito.mock(OIDCClient.class);
        Mockito.when(oidcClient.authoriseSession(Mockito.any()))
                .thenThrow(new AuthorisationException(new Exception()));

        var sessionManager = new SessionManager(oidcClient);

        Assertions.assertThatThrownBy(() -> sessionManager.createSession(new AuthorizationCode()))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void throwsErrorIfUserInfoRequestFails() throws Exception {
        var oidcClient = Mockito.mock(OIDCClient.class);
        var session = Mockito.mock((Session.class));
        Mockito.when(session.getAccessTokenHash()).thenReturn("Access token");
        Mockito.when(session.getSubClaim()).thenReturn("Sub claim");
        Mockito.when(oidcClient.authoriseSession(Mockito.any())).thenReturn(session);
        Mockito.when(oidcClient.fetchUserInfo(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new UserInfoFetchingException("User info exception"));

        var sessionManager = new SessionManager(oidcClient);

        Assertions.assertThatThrownBy(() -> sessionManager.createSession(new AuthorizationCode()))
                .isInstanceOf(UserInfoFetchingException.class);
    }
}
