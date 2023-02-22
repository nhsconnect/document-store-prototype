package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginHandler.class);
    public static final int SEE_OTHER_STATUS_CODE = 303;
    private final AuthenticationRequestFactory authenticationRequestFactory;
    private final UUIDProvider uuidProvider;

    @SuppressWarnings("unused")
    public LoginHandler() {
        this(new AuthenticationRequestFactory(new OIDCClientConfig()), new UUIDProvider());
    }

    public LoginHandler(
            AuthenticationRequestFactory authenticationRequestFactory, UUIDProvider uuidProvider) {
        this.authenticationRequestFactory = authenticationRequestFactory;
        this.uuidProvider = uuidProvider;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        // TODO: Create a random and secure new session ID,
        //  store the session identifier
        //  set the OIDC request state in Dynamo for use in callback route
        //  set an expiry so that incomplete sessions are not left cached

        var authRequest = authenticationRequestFactory.build();
        LOGGER.debug("Redirecting user to " + authRequest.toURI().toString());
        var sessionId = uuidProvider.generateUUID();
        var headers =
                Map.of(
                        "Location",
                        authRequest.toURI().toString(),
                        "Set-Cookie",
                        "SessionId=" + sessionId + "; SameSite=Strict; Secure; HttpOnly");

        var response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);
        response.setIsBase64Encoded(false);
        response.setBody("");

        return response;
    }
}
