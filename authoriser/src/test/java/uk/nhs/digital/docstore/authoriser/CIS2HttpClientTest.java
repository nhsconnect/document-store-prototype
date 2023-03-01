package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.*;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.time.Instant;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class CIS2HttpClientTest {

    @Test
    void createsAUserSessionWhenTheAuthCodeCanBeExchangedForAValidIdToken() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);

        var claimsSet = buildClaimsSet();
        var idToken = new PlainJWT(claimsSet);
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(idToken);

        var tokenValidator =
                new IDTokenValidator(
                        Issuer.parse(claimsSet.getIssuer()),
                        new ClientID(claimsSet.getAudience().get(0)));

        var client = new CIS2HttpClient(sessionStore, tokenFetcher, tokenValidator);

        Session result;
        try {
            result = client.authoriseSession(authCode);
        } catch (AuthorisationException e) {
            throw new RuntimeException(e);
        }

        var optionalSession = sessionStore.load(result.getId());
        Assertions.assertThat(optionalSession).isPresent();
        var session = optionalSession.get();

        Assertions.assertThat(session.getTimeToExist())
                .isEqualTo(claimsSet.getExpirationTime().getTime());
    }

    @Test
    void throwsAnAuthorisationExceptionWhenTheIDTokenIsNotValid() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);

        var claimsSet = buildClaimsSet();
        var idToken = new PlainJWT(claimsSet);
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(idToken);

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client = new CIS2HttpClient(sessionStore, tokenFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void throwsAuthorisationExceptionWhenFetchingTheIdentityTokenFails() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);

        Mockito.when(tokenFetcher.fetchToken(authCode)).thenThrow(new TokenFetchingException());

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client = new CIS2HttpClient(sessionStore, tokenFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }

    private JWTClaimsSet buildClaimsSet() {
        var issuer = "http://issuer.url";
        var clientID = new ClientID("test");
        var issuedAt = Instant.now().getEpochSecond();
        var expirationTime = Instant.now().plusSeconds(500).getEpochSecond();
        var sub = UUID.randomUUID();
        var claimsSetBuilder = new JWTClaimsSet.Builder();
        return claimsSetBuilder
                .claim(JWTClaimNames.EXPIRATION_TIME, expirationTime)
                .claim(JWTClaimNames.ISSUER, issuer)
                .claim(JWTClaimNames.SUBJECT, sub.toString())
                .claim(JWTClaimNames.AUDIENCE, clientID.toString())
                .claim(JWTClaimNames.ISSUED_AT, issuedAt)
                .build();
    }
}
