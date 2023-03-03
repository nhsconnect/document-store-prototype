package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.net.MalformedURLException;
import java.util.HashMap;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requests.TokenRequestEvent;

public class TokenRequestHandler extends BaseAuthRequestHandler
        implements RequestHandler<TokenRequestEvent, APIGatewayProxyResponseEvent> {

    private final OIDCClient OIDCClient;

    @SuppressWarnings("unused")
    public TokenRequestHandler() {
        this(
                new OIDCHttpClient(
                        new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())),
                        new OIDCTokenFetcher(getClientInformation(), null, getProviderMetadata()),
                        makeIDTokenValidator()));
    }

    public TokenRequestHandler(OIDCClient OIDCClient) {
        this.OIDCClient = OIDCClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(TokenRequestEvent input, Context context) {
        var authCode = input.getAuthCode();
        if (authCode.isEmpty()) {
            throw new RuntimeException("Auth code is empty");
        }
        var queryParameterState = input.getQueryParameterState();
        var cookieState = input.getCookieState();
        if (queryParameterState.isEmpty()
                || cookieState.isEmpty()
                || !queryParameterState.get().equals(cookieState.get())) {
            var invalidStateResponse = new APIGatewayProxyResponseEvent();
            invalidStateResponse.setStatusCode(400);
            invalidStateResponse.setIsBase64Encoded(false);
            invalidStateResponse.setBody("");
            return invalidStateResponse;
        }
        Session session;
        try {
            session = OIDCClient.authoriseSession(authCode.get(), new Nonce());
        } catch (AuthorisationException e) {
            throw new RuntimeException(e);
        }

        var headers = new HashMap<String, String>();
        headers.put(
                "Location", input.getRedirectUri().orElseThrow() + "?Role=" + session.getRole());
        headers.put(
                "Set-Cookie",
                "State="
                        + input.getCookieState().orElseThrow()
                        + "; SameSite=Strict; Secure; HttpOnly; Max-Age=0");

        var response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);

        return response;
    }

    private static IDTokenValidator makeIDTokenValidator() {
        var clientInfo = getClientInformation();
        var providerMetadata = getProviderMetadata();
        try {
            return new IDTokenValidator(
                    providerMetadata.getIssuer(),
                    clientInfo.getID(),
                    JWSAlgorithm.RS256,
                    providerMetadata.getJWKSetURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
