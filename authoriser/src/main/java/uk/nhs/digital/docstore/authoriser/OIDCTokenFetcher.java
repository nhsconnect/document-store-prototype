package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import java.util.List;
import java.util.Map;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;

public class OIDCTokenFetcher {
    private final ClientInformation clientInfo;
    private TokenRequestClient oidcClient;
    private OIDCProviderMetadata providerMetadata;

    public OIDCTokenFetcher(
            ClientInformation clientInfo,
            TokenRequestClient oidcClient,
            OIDCProviderMetadata providerMetadata) {
        this.clientInfo = clientInfo;
        this.oidcClient = oidcClient;
        this.providerMetadata = providerMetadata;
    }

    public JWT fetchToken(AuthorizationCode authCode, Nonce nonce) throws TokenFetchingException {
        var tokenEndpoint = providerMetadata.getTokenEndpointURI();
        var clientAuth = new ClientSecretPost(clientInfo.getID(), clientInfo.getSecret());
        var codeGrant =
                new AuthorizationCodeGrant(authCode, clientInfo.getMetadata().getRedirectionURI());
        var customParams = Map.of("nonce", List.of(nonce.getValue()));
        TokenRequest tokenRequest =
                new TokenRequest(tokenEndpoint, clientAuth, codeGrant, null, null, customParams);
        var tokenResponse = oidcClient.getResponse(tokenRequest);

        if (!tokenResponse.indicatesSuccess()) {
            // We got an error response...
            TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
            throw new TokenFetchingException(errorResponse.getErrorObject().getDescription());
        }
        return tokenResponse.toSuccessResponse().getTokens().toOIDCTokens().getIDToken();
    }
}
