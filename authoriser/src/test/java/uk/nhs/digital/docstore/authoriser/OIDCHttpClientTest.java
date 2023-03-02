package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.*;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class OIDCHttpClientTest {
    @Test
    void createsAUserSessionWhenTheAuthCodeCanBeExchangedForAValidIdToken() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);

        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var idToken = new PlainJWT(claimsSet);
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(idToken);

        var tokenValidator =
                new IDTokenValidator(
                        Issuer.parse(claimsSet.getIssuer()),
                        new ClientID(claimsSet.getAudience().get(0)));

        var client = new OIDCHttpClient(sessionStore, tokenFetcher, tokenValidator);

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

        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var idToken = new PlainJWT(claimsSet);
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(idToken);

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client = new OIDCHttpClient(sessionStore, tokenFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void throwsAuthorisationExceptionWhenFetchingTheIdentityTokenFails() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);

        Mockito.when(tokenFetcher.fetchToken(authCode))
                .thenThrow(new TokenFetchingException("error"));

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client = new OIDCHttpClient(sessionStore, tokenFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }
}
