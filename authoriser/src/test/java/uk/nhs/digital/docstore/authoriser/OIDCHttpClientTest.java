package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.*;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.time.Instant;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.builders.IDTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class OIDCHttpClientTest {
    @Disabled("Disabled until fixed")
    @Test
    void createsAUserSessionWhenTheAuthCodeCanBeExchangedForAValidIdToken() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);

        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var idToken = new PlainJWT(claimsSet.toJWTClaimsSet());
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(idToken);

        var tokenValidator =
                new IDTokenValidator(
                        claimsSet.getIssuer(), new ClientID(claimsSet.getAudience().get(0)));

        var client =
                new OIDCHttpClient(sessionStore, tokenFetcher, userInfoFetcher, tokenValidator);

        Session result;
        try {
            result = client.authoriseSession(authCode);
        } catch (AuthorisationException e) {
            throw new RuntimeException(e);
        }

        var optionalSession =
                sessionStore.load(new Subject(result.getOIDCSubject()), result.getId());
        Assertions.assertThat(optionalSession).isPresent();
        var session = optionalSession.get();

        Assertions.assertThat(session.getTimeToExist())
                .isEqualTo(Instant.ofEpochMilli(claimsSet.getExpirationTime().getTime()));

        Assertions.assertThat(session.getOIDCSubject())
                .isEqualTo(claimsSet.getSubject().getValue());
        Assertions.assertThat(session.getOidcSessionID())
                .isEqualTo(claimsSet.getSessionID().getValue());
    }

    @Test
    void throwsAnAuthorisationExceptionWhenTheIDTokenIsNotValid() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);

        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var idToken = new PlainJWT(claimsSet.toJWTClaimsSet());
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(idToken);

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client =
                new OIDCHttpClient(sessionStore, tokenFetcher, userInfoFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void throwsAuthorisationExceptionWhenFetchingTheIdentityTokenFails() throws Exception {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);

        Mockito.when(tokenFetcher.fetchToken(authCode))
                .thenThrow(new TokenFetchingException("error"));

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client =
                new OIDCHttpClient(sessionStore, tokenFetcher, userInfoFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void returnsUserInfoWhenGivenAValidSessionId() throws Exception {
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);
        var clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var sessionId = "sessionId";
        var expectedUserInfo = new UserInfo(new Subject());
        Mockito.when(userInfoFetcher.fetchUserInfo(new BearerAccessToken(sessionId)))
                .thenReturn(expectedUserInfo);
        var client =
                new OIDCHttpClient(sessionStore, tokenFetcher, userInfoFetcher, tokenValidator);

        var result = client.fetchUserInfo(sessionId);

        assert (result).equals(expectedUserInfo);
    }
}
