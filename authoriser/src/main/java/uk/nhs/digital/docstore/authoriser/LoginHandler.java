package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final Clock clock;

    @SuppressWarnings("unused")
    public LoginHandler(Clock clock) {
        this(
                new AuthenticationRequestFactory(new OIDCClientConfig()),
                new DynamoDBSessionStore(),
                new UUIDProvider(),
                clock);
    }

    public LoginHandler(
            AuthenticationRequestFactory authenticationRequestFactory,
            SessionStore sessionStore,
            UUIDProvider uuidProvider,
            Clock clock) {
        this.authenticationRequestFactory = authenticationRequestFactory;
        this.sessionStore = sessionStore;
        this.uuidProvider = uuidProvider;
        this.clock = clock;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        var authRequest = authenticationRequestFactory.build();
        LOGGER.debug("Redirecting user to " + authRequest.toURI().toString());

        var sessionId = uuidProvider.generateUUID();
        var timeToExit = Instant.now(clock).plus(1, ChronoUnit.HOURS).getEpochSecond();
        var session =
                Session.create(
                        sessionId.toString(),
                        BigInteger.valueOf(timeToExit),
                        authRequest.getState().toString());
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
