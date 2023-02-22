package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.math.BigInteger;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class LoginHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginHandler.class);
    public static final int SEE_OTHER_STATUS_CODE = 303;
    private final AuthenticationRequestFactory authenticationRequestFactory;
    private final SessionStore sessionStore;
    private final UUIDProvider uuidProvider;

    @SuppressWarnings("unused")
    public LoginHandler() {
        this(
                new AuthenticationRequestFactory(new OIDCClientConfig()),
                new DynamoDBSessionStore(),
                new UUIDProvider());
    }

    public LoginHandler(
            AuthenticationRequestFactory authenticationRequestFactory,
            SessionStore sessionStore,
            UUIDProvider uuidProvider) {
        this.authenticationRequestFactory = authenticationRequestFactory;
        this.sessionStore = sessionStore;
        this.uuidProvider = uuidProvider;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        // TODO:
        //  set an expiry so that incomplete sessions are not left cached

        var authRequest = authenticationRequestFactory.build();
        LOGGER.debug("Redirecting user to " + authRequest.toURI().toString());

        var sessionId = uuidProvider.generateUUID();
        var session =
                Session.create(
                        sessionId.toString(), BigInteger.ONE, authRequest.getState().toString());
        sessionStore.save(session);

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
