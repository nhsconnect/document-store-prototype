package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.util.URLUtils;
import com.nimbusds.openid.connect.sdk.claims.LogoutTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;
import java.text.ParseException;

@SuppressWarnings("unused")
public class BackChannelLogoutHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final SessionStore sessionStore;
    private final LogoutTokenValidator tokenValidator;

    public BackChannelLogoutHandler(
            LogoutTokenValidator tokenValidator, SessionStore sessionStore) {
        this.sessionStore = sessionStore;
        this.tokenValidator = tokenValidator;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {
        var parameters = URLUtils.parseParameters(requestEvent.getBody());
        var rawLogoutToken = parameters.get("logout_token").get(0);

        JWT logoutToken;

        try {
            logoutToken = JWTParser.parse(rawLogoutToken);
        } catch (ParseException exception) {
            throw new RuntimeException(exception);
        }

        LogoutTokenClaimsSet claims;

        try {
            claims = tokenValidator.validate(logoutToken);
        } catch (BadJOSEException | JOSEException exception) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("")
                    .withIsBase64Encoded(false);
        }

        var sessions = sessionStore.queryByOIDCSubject(claims.getSubject());

        sessionStore.batchDelete(sessions);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withIsBase64Encoded(false)
                .withBody("");
    }
}
