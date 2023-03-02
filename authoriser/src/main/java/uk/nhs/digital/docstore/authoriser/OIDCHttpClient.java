package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.util.UUID;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class OIDCHttpClient implements OIDCClient {

    private final SessionStore sessionStore;
    private final OIDCTokenFetcher tokenFetcher;
    private final IDTokenValidator tokenValidator;

    public OIDCHttpClient(
            SessionStore sessionStore,
            OIDCTokenFetcher tokenFetcher,
            IDTokenValidator tokenValidator) {
        this.sessionStore = sessionStore;
        this.tokenFetcher = tokenFetcher;
        this.tokenValidator = tokenValidator;
    }

    @Override
    public Session authoriseSession(AuthorizationCode authCode) throws AuthorisationException {
        JWT token;
        try {
            token = tokenFetcher.fetchToken(authCode);
        } catch (TokenFetchingException e) {
            throw new AuthorisationException(e);
        }

        IDTokenClaimsSet claimsSet;
        try {
            // TODO: Add nonce validation
            claimsSet = tokenValidator.validate(token, null);
        } catch (BadJOSEException | JOSEException e) {
            throw new AuthorisationException(e);
        }

        var session = Session.create(UUID.randomUUID(), claimsSet.getExpirationTime().getTime());
        sessionStore.save(session);
        return session;
    }
}
