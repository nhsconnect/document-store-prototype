package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;

public class OIDCTokenFetcher {
    private final ClientInformation clientInfo;
    private final TokenRequestClient oidcClient;
    private final OIDCProviderMetadata providerMetadata;

    public OIDCTokenFetcher(
            ClientInformation clientInfo,
            TokenRequestClient oidcClient,
            OIDCProviderMetadata providerMetadata) {
        this.clientInfo = clientInfo;
        this.oidcClient = oidcClient;
        this.providerMetadata = providerMetadata;
    }

    public OIDCTokens fetchToken(AuthorizationCode authCode) throws TokenFetchingException {
        var tokenEndpoint = providerMetadata.getTokenEndpointURI();
        var clientAuth = new ClientSecretPost(clientInfo.getID(), clientInfo.getSecret());
        var codeGrant =
                new AuthorizationCodeGrant(authCode, clientInfo.getMetadata().getRedirectionURI());
        TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
        var tokenResponse = oidcClient.getResponse(tokenRequest);

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            throw new TokenFetchingException(errorResponse.getErrorObject().getDescription());
        }
        var successResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
        return successResponse.getOIDCTokens();
    }
}
