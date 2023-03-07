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
    private final LogoutTokenValidator validator;

    public BackChannelLogoutHandler(LogoutTokenValidator validator, SessionStore sessionStore) {
        this.sessionStore = sessionStore;
        this.validator = validator;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        var parameters = URLUtils.parseParameters(input.getBody());
        var rawToken = parameters.get("logout_token").get(0);

        JWT token;
        try {
            token = JWTParser.parse(rawToken);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        LogoutTokenClaimsSet claims;
        try {
            claims = validator.validate(token);
        } catch (BadJOSEException | JOSEException e) {
            var invalidTokenResponse = new APIGatewayProxyResponseEvent();
            invalidTokenResponse.setStatusCode(400);
            invalidTokenResponse.setBody("");
            invalidTokenResponse.setIsBase64Encoded(false);
            return invalidTokenResponse;
        }

        var sessions = sessionStore.queryByOIDCSubject(claims.getSubject());
        // TODO: Enable deletion of multiple sessions by providing a batch delete operation
        sessionStore.delete(sessions.get(0));

        var response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setIsBase64Encoded(false);
        response.setBody("");
        return response;
    }
}
