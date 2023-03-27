package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.LogoutRequestEvent;

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
    public APIGatewayProxyResponseEvent handleRequest(
            LogoutRequestEvent requestEvent, Context context) {
        var sessionId = requestEvent.getSessionId();
        var subject = requestEvent.getSubject();
        var multiValueHeaders = new HashMap<String, List<String>>();
        // TODO: [PRMT-2779] Improve redaction if it is required
        var redactedSessionId =
                sessionId.isEmpty()
                        ? "null"
                        : sessionId.toString().substring(sessionId.toString().length() - 4);

        LOGGER.debug("Logging out with session ID ending in: " + redactedSessionId);

        if (sessionId.isPresent() && subject.isPresent()) {
            var session = sessionStore.load(subject.get(), sessionId.get());

            if (session.isPresent()) {
                LOGGER.debug("Deleting session with ID ending in: " + redactedSessionId);

                sessionStore.delete(session.get());

                LOGGER.debug(
                        "Successfully deleted session with ID ending in: " + redactedSessionId);
            }

            var subjectClaimCookie =
                    httpOnlyCookieBuilder("SubjectClaim", subject.get().getValue(), 0L);
            var sessionIdCookie =
                    httpOnlyCookieBuilder("SessionId", sessionId.get().toString(), 0L);
            var loggedInCookie = cookieBuilder("LoggedIn", "", 0L);

            multiValueHeaders.put(
                    "Set-Cookie", List.of(sessionIdCookie, subjectClaimCookie, loggedInCookie));
        }

        var headers = new HashMap<String, String>();
        headers.put("Location", requestEvent.getRedirectUri().orElseThrow());

        LOGGER.debug("Redirecting to " + headers.get("Location"));

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withStatusCode(SEE_OTHER_STATUS_CODE)
                .withHeaders(headers)
                .withMultiValueHeaders(multiValueHeaders)
                .withBody("");
    }
}
