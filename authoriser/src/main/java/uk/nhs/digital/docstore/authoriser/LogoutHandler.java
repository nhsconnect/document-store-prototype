package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.requests.LogoutRequestEvent;

public class LogoutHandler extends BaseAuthRequestHandler
        implements RequestHandler<LogoutRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LogoutHandler.class);

    private final SessionStore sessionStore;

    public LogoutHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @SuppressWarnings("unused")
    public LogoutHandler() {
        this(new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(LogoutRequestEvent input, Context context) {
        var sessionId = input.getSessionId();

        LOGGER.debug("Deleting session " + sessionId);

        sessionId.ifPresent(sessionStore::delete);

        LOGGER.debug("Successfully deleted session " + sessionId);

        var headers = new HashMap<String, String>();
        headers.put("Location", input.getRedirectUri().orElseThrow());
        sessionId.ifPresent(
                uuid -> headers.put("Set-Cookie", "SessionId=" + uuid + "; Path=/; Max-Age=0"));

        LOGGER.debug("Redirecting to " + headers.get("Location"));

        var response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);
        response.setBody("");

        return response;
    }
}

// delete the session id
