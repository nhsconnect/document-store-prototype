package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class LoginHandler extends BaseAuthRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginHandler.class);
    private final AuthenticationRequestFactory authenticationRequestFactory;
    private final SessionStore sessionStore;
    private final UUIDProvider uuidProvider;
    private final Clock clock;

    @SuppressWarnings("unused")
    public LoginHandler() {
        this(
                new AuthenticationRequestFactory(new OIDCClientConfig()),
                new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())),
                new UUIDProvider(),
                Clock.systemUTC());
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
        var timeToExist = Instant.now(clock).plus(1, ChronoUnit.HOURS).getEpochSecond();
        var session = Session.create(sessionId, timeToExist, authRequest.getState());

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
