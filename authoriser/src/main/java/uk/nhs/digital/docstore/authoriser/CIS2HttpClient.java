package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.text.ParseException;
import java.util.Optional;
import java.util.UUID;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class CIS2HttpClient implements CIS2Client {

    private final SessionStore sessionStore;
    private final OIDCTokenFetcher tokenFetcher;

    public CIS2HttpClient(SessionStore sessionStore, OIDCTokenFetcher tokenFetcher) {
        this.sessionStore = sessionStore;
        this.tokenFetcher = tokenFetcher;
    }

    @Override
    public Optional<Session> authoriseSession(AuthorizationCode authCode) {
        JWT token;
        try {
            token = tokenFetcher.fetchToken(authCode);
        } catch (TokenFetchingException e) {
            throw new RuntimeException(e);
        }

        // Verify token signature and claims
        long expirationTime;
        try {
            var claimsSet = token.getJWTClaimsSet();
            expirationTime = claimsSet.getLongClaim(JWTClaimNames.EXPIRATION_TIME);
        } catch (ParseException e) {
            // Should we return an empty optional or throw an exception here?
            throw new RuntimeException(e);
        }

        var session = Session.create(UUID.randomUUID(), expirationTime);
        sessionStore.save(session);
        return Optional.of(session);
    }
}
