package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.apiRequestClients.UserInfoFetcher;
import uk.nhs.digital.docstore.authoriser.apiRequestClients.UserInfoRequestClient;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;

class UserInfoFetcherTest {

    @Test
    void fetchTokenReturnsTheIDTokenIfTheRequestIsSuccessful() throws Exception {
        var user = new Subject();
        var userInfo = new UserInfo(user);
        var orgCodeKey = "org_code";
        var orgCode = "8JD29";
        var roleCodeKey = "role_code";
        var roleCode = "R8000";
        userInfo.setClaim(orgCodeKey, orgCode);
        userInfo.setClaim(roleCodeKey, roleCode);
        var userInfoEndpoint = new FakeUserInfoRequestClient(userInfo);

        var userInfoURI = new URI("http://user-info.uri");

        var providerMetadata =
                new OIDCProviderMetadata(
                        new Issuer("test"),
                        List.of(SubjectType.PUBLIC),
                        new URI("http://uri.jwks"));
        providerMetadata.setUserInfoEndpointURI(userInfoURI);

        var fetcher = new UserInfoFetcher(userInfoEndpoint, providerMetadata);

        var returnedInfo = fetcher.fetchUserInfo(new BearerAccessToken());

        var latestRequest = userInfoEndpoint.getLatestRequest();
        assertThat(latestRequest).isNotNull();

        assertThat(returnedInfo.getClaim(roleCodeKey)).isEqualTo(roleCode);
        assertThat(returnedInfo.getClaim(orgCodeKey)).isEqualTo(orgCode);

        // Assert that token request has correct token endpoint configured
        assertThat(latestRequest.getEndpointURI()).isEqualTo(userInfoURI);

        //        // Assert token request has client ID secret included as parameters
        //        // no method to retrieve secret
        //
        // assertThat(latestRequest.getClientAuthentication().getClientID()).isEqualTo(clientID);
        //        // Assert that the result token is the same token we passed into our fake token
        // request
        //        // client constructor
        //        assertThat(result).isEqualTo(token);
    }

    @Test
    void shouldThrowUserInfoFetchingExceptionWhenGettingErrorResponse() throws URISyntaxException {
        var userInfoEndpoint = new FakeUserInfoRequestClient(null);

        var userInfoURI = new URI("http://user-info.uri");

        var providerMetadata =
                new OIDCProviderMetadata(
                        new Issuer("test"),
                        List.of(SubjectType.PUBLIC),
                        new URI("http://uri.jwks"));
        providerMetadata.setUserInfoEndpointURI(userInfoURI);

        var fetcher = new UserInfoFetcher(userInfoEndpoint, providerMetadata);

        assertThrows(
                UserInfoFetchingException.class,
                () -> fetcher.fetchUserInfo(new BearerAccessToken("some-code")));
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
                return new UserInfoSuccessResponse(userInfo) {};
            } else {
                return new UserInfoErrorResponse(new ErrorObject("error", "Some error"));
            }
        }
    }
}
