package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.requests.LogoutRequestEvent;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

public class LogoutHandler extends BaseAuthRequestHandler
        implements RequestHandler<LogoutRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LogoutHandler.class);

    private final SessionStore sessionStore;

    public LogoutHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public LogoutHandler() {
        this(new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(LogoutRequestEvent input, Context context) {
        var sessionId = input.getSessionId();

        LOGGER.debug("Deleting session " + sessionId);

        sessionId.ifPresent(sessionStore::delete);

        LOGGER.debug("Successfully deleted session " + sessionId);

        var response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        var headers = new HashMap<String, String>();
        try {
            headers.put("Location", input.getRedirectUri().orElseThrow());
            sessionId.ifPresent(uuid -> headers.put("Set-Cookie", "SessionId=" + uuid + "; Path=/; Max-Age=0"));
        } catch (NoSuchElementException e) {
            LOGGER.error("Logout request is missing query parameter: redirect_uri");
            headers.put("Content-Type", "text/html");
            response.setStatusCode(400);
            response.setBody("<html><head></head><body><p>Missing query parameter: redirect_uri</p></body></html>");
            return response;
        }

        LOGGER.debug("Redirecting to " + headers.get("Location"));

        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);
        response.setBody("");

        return response;
    }
}

//delete the session id
