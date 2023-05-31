package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import net.minidev.json.JSONObject;
import uk.nhs.digital.docstore.authoriser.TokenRequestClient;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;

public class UserInfoFetcher {
    private final ClientInformation clientInfo;
    private final TokenRequestClient oidcClient;
    private final OIDCProviderMetadata providerMetadata;

    public UserInfoFetcher(
            ClientInformation clientInfo,
            TokenRequestClient oidcClient,
            OIDCProviderMetadata providerMetadata) {
        this.clientInfo = clientInfo;
        this.oidcClient = oidcClient;
        this.providerMetadata = providerMetadata;
    }

    public JSONObject fetchUserInfo(AuthorizationCode authCode) throws TokenFetchingException {
        var userInfoEndpoint = providerMetadata.getUserInfoEndpointURI();
        var clientAuth = new ClientSecretPost(clientInfo.getID(), clientInfo.getSecret());
        var codeGrant =
                new AuthorizationCodeGrant(authCode, clientInfo.getMetadata().getRedirectionURI());
        TokenRequest tokenRequest = new TokenRequest(userInfoEndpoint, clientAuth, codeGrant);
        var tokenResponse = oidcClient.getResponse(tokenRequest);

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            throw new TokenFetchingException(errorResponse.getErrorObject().getDescription());
        }
        var successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        return successResponse.getOIDCTokens().getIDToken();
    }
}
