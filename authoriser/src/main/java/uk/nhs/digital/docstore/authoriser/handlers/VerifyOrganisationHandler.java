package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.Utils;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.OrganisationRequestEvent;

public class VerifyOrganisationHandler extends BaseAuthRequestHandler
        implements RequestHandler<OrganisationRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(VerifyOrganisationHandler.class);
    private final SessionStore sessionStore;
    private final String ORG = "organisation";

    public VerifyOrganisationHandler() {
        this(new DynamoDBSessionStore(createDynamoDbMapper()));
    }

    public VerifyOrganisationHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            OrganisationRequestEvent input, Context context) {
        var odsCode = Utils.getValueFromQueryStringParams(input.getQueryStringParameters(), ORG);
        var sessionId = input.getSessionId();
        var subjectClaim = input.getSubjectClaim();

        if (odsCode.isEmpty() || sessionId.isEmpty() || subjectClaim.isEmpty()) {
            LOGGER.error("ODS code, SessionId or SubjectClaim are missing");
            return orgHandlerError(400);
        }

        try {
            var session =
                    sessionStore.queryBySessionIdWithKeys(
                            subjectClaim.get(), sessionId.get().toString());

            if (session.isEmpty()) {
                LOGGER.error(
                        "Unable to find Session using the provided SessionId and SubjectClaim");
                return orgHandlerError(404);
            }

            var match =
                    session.get().getOrganisations().stream()
                            .anyMatch(org -> org.getOdsCode().equals(odsCode.get()));

            if (!match) {
                LOGGER.error("ODS code did not match against user session");
                return orgHandlerError(404);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return orgHandlerError(400);
        }

        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Origin", Utils.getAmplifyBaseUrl());

        var response = new JSONObject();
        response.put("org", "test");

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withHeaders(headers)
                .withBody(response.toString());
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