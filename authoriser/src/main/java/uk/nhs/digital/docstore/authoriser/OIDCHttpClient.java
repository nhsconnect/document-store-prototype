package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.time.Instant;
import java.util.UUID;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;

public class OIDCHttpClient implements OIDCClient {

    private final SessionStore sessionStore;
    private final OIDCTokenFetcher tokenFetcher;
    private final UserInfoFetcher userInfoFetcher;
    private final IDTokenValidator tokenValidator;

    public OIDCHttpClient(
            SessionStore sessionStore,
            OIDCTokenFetcher tokenFetcher,
            UserInfoFetcher userInfoFetcher,
            IDTokenValidator tokenValidator) {
        this.sessionStore = sessionStore;
        this.tokenFetcher = tokenFetcher;
        this.userInfoFetcher = userInfoFetcher;
        this.tokenValidator = tokenValidator;
    }

    @Override
    public Session authoriseSession(AuthorizationCode authCode) throws AuthorisationException {
        OIDCTokens oidcAuthResponse;
        try {
            oidcAuthResponse = tokenFetcher.fetchToken(authCode);
        } catch (TokenFetchingException e) {
            throw new AuthorisationException(e);
        }

        JWT IDToken = oidcAuthResponse.getIDToken();
        IDTokenClaimsSet claimsSet;

        try {
            claimsSet = tokenValidator.validate(IDToken, null);
        } catch (BadJOSEException | JOSEException e) {
            throw new AuthorisationException(e);
        }

        var session =
                Session.create(
                        UUID.randomUUID(),
                        Instant.ofEpochMilli(claimsSet.getExpirationTime().getTime()),
                        claimsSet.getSubject(),
                        claimsSet.getSessionID(),
                        oidcAuthResponse.getAccessToken());
        sessionStore.save(session);
        return session;
    }

    @Override
    public UserInfo fetchUserInfo(String sessionID) throws AuthorisationException {
        UserInfo userInfo;
        try {
            userInfo = userInfoFetcher.fetchUserInfo(new BearerAccessToken(sessionID));
        } catch (UserInfoFetchingException e) {
            System.out.println("sessionID: " + sessionID);
            throw new AuthorisationException(e);
        }

        return userInfo;
    }
}
