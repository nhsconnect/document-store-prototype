package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.Utils;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;

public class VerifyOrganisationHandler extends BaseAuthRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(VerifyOrganisationHandler.class);
    private final SessionStore sessionStore;

    public VerifyOrganisationHandler() {
        this(new DynamoDBSessionStore(createDynamoDbMapper()));
    }

    public VerifyOrganisationHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        LOGGER.debug("Lambda hit");
        LOGGER.debug("Path Params: {}", input.getPathParameters());
        LOGGER.debug("Query Params: {}", input.getQueryStringParameters());
        LOGGER.debug("Body: {}", input.getBody());
        //        LOGGER.debug("Session ID: {}", input.getSessionId());

        //        try {
        //            var session =
        //                    sessionStore.queryBySessionId(new
        // SessionID(input.getSessionId().toString()));
        //            LOGGER.debug(session.toString());
        //        } catch (Exception e) {
        //            return orgHandlerError(400);
        //        }

        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Origin", Utils.getAmplifyBaseUrl());

        var response = new JSONObject();
        response.put("org", "test");

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withHeaders(headers)
                .withBody("");
    }

    private static APIGatewayProxyResponseEvent orgHandlerError(int statusCode) {
        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody("");
    }
}
