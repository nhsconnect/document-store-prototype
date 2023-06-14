package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class SessionManager {
    OIDCClient authenticationClient;

    public SessionManager(OIDCClient authenticationClient) {
        this.authenticationClient = authenticationClient;
    }

    public Session createSession(AuthorizationCode authCode)
            throws AuthorisationException, UserInfoFetchingException {
        var session = authenticationClient.authoriseSession(authCode);
        var userInfo =
                authenticationClient.fetchUserInfo(
                        session.getAccessTokenHash(), session.getSubClaim());
        System.out.println(userInfo.toJSONString());

        return session;
    }
}
