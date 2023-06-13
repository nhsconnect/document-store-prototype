package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;

public class UserInfoFetcher {
    private final UserInfoRequestClient userInfoClient;
    private final OIDCProviderMetadata providerMetadata;

    public UserInfoFetcher(
            UserInfoRequestClient oidcClient, OIDCProviderMetadata providerMetadata) {
        this.userInfoClient = oidcClient;
        this.providerMetadata = providerMetadata;
    }

    public UserInfo fetchUserInfo(AccessToken authCode) throws UserInfoFetchingException {
        var userInfoEndpoint = providerMetadata.getUserInfoEndpointURI();
        UserInfoRequest userInfoRequest = new UserInfoRequest(userInfoEndpoint, authCode);

        var userInfoResponse = userInfoClient.getResponse(userInfoRequest);

        if (!userInfoResponse.indicatesSuccess()) {
            // We got an error response...
            UserInfoErrorResponse errorResponse = userInfoResponse.toErrorResponse();
            var error = errorResponse.getErrorObject();
            throw new UserInfoFetchingException(
                    "Status Code: " + error.getHTTPStatusCode() + " " + error.getDescription());
        }
        return userInfoResponse.toSuccessResponse().getUserInfo();
    }
}
