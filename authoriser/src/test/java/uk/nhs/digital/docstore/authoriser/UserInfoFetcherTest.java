package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.oauth2.sdk.client.ClientMetadata;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.builders.IDTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserInfoFetcherTest {

    @Test
    void fetchTokenReturnsTheIDTokenIfTheRequestIsSuccessful() throws Exception {
        var token = new PlainJWT(IDTokenClaimsSetBuilder.buildClaimsSet().toJWTClaimsSet());

        var oidcClient =
                new FakeUserInfoRequestClient(new OIDCTokens(token, new BearerAccessToken(), null));

        var clientID = new ClientID("client-id");
        var secret = new Secret("some-secret");
        var redirectURI = new URI("http://some-redirect.uri");
        var tokenURI = new URI("http://token.uri");
        var clientMetadata = new ClientMetadata();
        clientMetadata.setRedirectionURI(redirectURI);
        var clientInfo = new ClientInformation(clientID, null, clientMetadata, secret);
        var providerMetadata =
                new OIDCProviderMetadata(
                        new Issuer("test"),
                        List.of(SubjectType.PUBLIC),
                        new URI("http://uri.jwks"));
        providerMetadata.setTokenEndpointURI(tokenURI);

        var fetcher = new OIDCTokenFetcher(clientInfo, oidcClient, providerMetadata);

        var authCode = new AuthorizationCode("test");
        var expectedAuthGrant = new AuthorizationCodeGrant(authCode, redirectURI);

        var result = fetcher.fetchToken(authCode);

        var latestRequest = oidcClient.getLatestRequest();
        // Assert that request was sent
        assertThat(latestRequest).isNotNull();
        // Assert that token request has correct token endpoint configured
        assertThat(latestRequest.getEndpointURI()).isEqualTo(tokenURI);
        // Assert token request has correct authorization grant
        assertThat(latestRequest.getAuthorizationGrant()).isEqualTo(expectedAuthGrant);
        // Assert token request has client ID secret included as parameters
        // no method to retrieve secret
        assertThat(latestRequest.getClientAuthentication().getClientID()).isEqualTo(clientID);
        // Assert that the result token is the same token we passed into our fake token request
        // client constructor
        assertThat(result).isEqualTo(token);
    }

    @Test
    void shouldThrowTokenFetchingExceptionWhenGettingErrorResponse()
            throws URISyntaxException, TokenFetchingException {
        var clientID = new ClientID("client-id");
        var secret = new Secret("some-secret");
        var redirectURI = new URI("http://some-redirect.uri");
        var tokenURI = new URI("http://token.uri");
        var oidcClient = new FakeUserInfoRequestClient(null);
        var clientMetadata = new ClientMetadata();
        clientMetadata.setRedirectionURI(redirectURI);
        var clientInfo = new ClientInformation(clientID, null, clientMetadata, secret);
        var providerMetadata =
                new OIDCProviderMetadata(
                        new Issuer("test"),
                        List.of(SubjectType.PUBLIC),
                        new URI("http://uri.jwks"));
        providerMetadata.setTokenEndpointURI(tokenURI);

        var fetcher = new OIDCTokenFetcher(clientInfo, oidcClient, providerMetadata);

        assertThrows(
                TokenFetchingException.class,
                () -> fetcher.fetchToken(new AuthorizationCode("some-code")));
    }

    private static class FakeUserInfoRequestClient implements UserInfoRequestClient {
        private final UserInfo userInfo;
        private List<UserInfoRequest> sentRequests = new ArrayList<>();

        public FakeUserInfoRequestClient(UserInfo info) {
            this.userInfo = info;
        }

        public UserInfoRequest getLatestRequest() {
            return sentRequests.get(sentRequests.size() - 1);
        }

        @Override
        public UserInfoResponse getResponse(UserInfoRequest request) {
            sentRequests.add(request);
            if (userInfo != null) {
                return new UserInfoResponse(userInfo) {
                };
            } else {
                return new TokenErrorResponse(new ErrorObject("error", "Some error"));
            }
        }
    }
}
