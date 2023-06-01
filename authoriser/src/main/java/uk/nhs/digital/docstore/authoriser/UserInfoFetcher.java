package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import net.minidev.json.JSONObject;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;

public class UserInfoFetcher {
    private final UserInfoRequestClient userInfoClient;
    private final OIDCProviderMetadata providerMetadata;

    public UserInfoFetcher(
            UserInfoRequestClient oidcClient, OIDCProviderMetadata providerMetadata) {
        this.userInfoClient = oidcClient;
        this.providerMetadata = providerMetadata;
    }

    public JSONObject fetchUserInfo(AccessToken authCode)
            throws UserInfoFetchingException, ParseException {
        var userInfoEndpoint = providerMetadata.getUserInfoEndpointURI();
        UserInfoRequest userInfoRequest = new UserInfoRequest(userInfoEndpoint, authCode);
        var userInfoResponse = userInfoClient.getResponse(userInfoRequest);

        if (!userInfoResponse.indicatesSuccess()) {
            // We got an error response...
            UserInfoErrorResponse errorResponse = userInfoResponse.toErrorResponse();
            throw new UserInfoFetchingException(errorResponse.getErrorObject().getDescription());
        }
        var successResponse = (UserInfoResponse) userInfoResponse.toSuccessResponse();
        return successResponse.toHTTPResponse().getContentAsJSONObject();
    }
}
