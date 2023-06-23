package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.*;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.builders.IDTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;

class OIDCHttpClientTest {
    @Test
    void createsAUserSessionWhenTheAuthCodeCanBeExchangedForAValidIdToken() throws Exception {
        var authCode = new AuthorizationCode();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);

        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var idToken = new PlainJWT(new PlainHeader(), claimsSet.toJWTClaimsSet());
        var accessToken = new BearerAccessToken();
        var oidcAuthResponse = new OIDCTokens(idToken, accessToken, null);
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(oidcAuthResponse);

        var tokenValidator =
                new IDTokenValidator(
                        claimsSet.getIssuer(), new ClientID(claimsSet.getAudience().get(0)));

        var client = new OIDCHttpClient(tokenFetcher, userInfoFetcher, tokenValidator);

        Session result;
        try {
            result = client.authoriseSession(authCode);
        } catch (AuthorisationException e) {
            throw new RuntimeException(e);
        }

        assert (result.getTimeToExist())
                .equals(Instant.ofEpochMilli(claimsSet.getExpirationTime().getTime()));
        assert (result.getOIDCSubject()).equals(claimsSet.getSubject().getValue());
        assert (result.getOidcSessionID()).equals(claimsSet.getSessionID().getValue());
        assert (result.getSubClaim()).equals(claimsSet.getClaim(JWTClaimNames.SUBJECT).toString());
        assert (result.getAccessTokenHash()).equals(accessToken.getValue());
    }

    @Test
    void throwsAnAuthorisationExceptionWhenTheIDTokenIsNotValid() throws Exception {
        var authCode = new AuthorizationCode();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);

        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var idToken = new PlainJWT(claimsSet.toJWTClaimsSet());
        var accessToken = new BearerAccessToken();
        var oidcAuthResponse = new OIDCTokens(idToken, accessToken, null);
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(oidcAuthResponse);

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client = new OIDCHttpClient(tokenFetcher, userInfoFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void throwsAuthorisationExceptionWhenFetchingTheIdentityTokenFails() throws Exception {
        var authCode = new AuthorizationCode();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);

        Mockito.when(tokenFetcher.fetchToken(authCode))
                .thenThrow(new TokenFetchingException("error"));

        ClientID clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var client = new OIDCHttpClient(tokenFetcher, userInfoFetcher, tokenValidator);

        Assertions.assertThatThrownBy(() -> client.authoriseSession(authCode))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void returnsUserInfoWhenGivenAValidSessionId() throws Exception {
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);
        var userInfoFetcher = Mockito.mock(UserInfoFetcher.class);
        var clientID = new ClientID("test");
        var tokenValidator = new IDTokenValidator(Issuer.parse("http://some.url"), clientID);

        var sessionId = "sessionId";
        var subClaim = "SubClaim";
        var expectedUserInfo = new UserInfo(new Subject());
        expectedUserInfo.setClaim(JWTClaimNames.SUBJECT, subClaim);
        Mockito.when(userInfoFetcher.fetchUserInfo(new BearerAccessToken(sessionId)))
                .thenReturn(expectedUserInfo);
        var client = new OIDCHttpClient(tokenFetcher, userInfoFetcher, tokenValidator);

        var result = client.fetchUserInfo(sessionId, subClaim);

        assert (result).equals(expectedUserInfo);
    }
}
