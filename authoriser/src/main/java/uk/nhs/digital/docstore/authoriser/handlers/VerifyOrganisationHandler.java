package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.id.Subject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.Utils;
import uk.nhs.digital.docstore.authoriser.audit.message.VerifyOrgBadRequestAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.message.VerifyOrgRequestAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.authoriser.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.authoriser.enums.HttpStatus;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.OrganisationRequestEvent;

public class VerifyOrganisationHandler extends BaseAuthRequestHandler
        implements RequestHandler<OrganisationRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(VerifyOrganisationHandler.class);
    private final SessionStore sessionStore;
    private final AuditPublisher sensitiveIndex;
    private Clock clock = Clock.systemUTC();
    private static final String ODS_CODE_PARAM_KEY = "odsCode";

    @SuppressWarnings("unused")
    public VerifyOrganisationHandler() {
        this(
                new DynamoDBSessionStore(createDynamoDbMapper()),
                new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")));
    }

    public VerifyOrganisationHandler(
            SessionStore sessionStore, Clock clock, AuditPublisher sensitiveIndex) {
        this(sessionStore, sensitiveIndex);
        this.clock = clock;
    }

    public VerifyOrganisationHandler(SessionStore sessionStore, AuditPublisher sensitiveIndex) {
        this.sessionStore = sessionStore;
        this.sensitiveIndex = sensitiveIndex;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            OrganisationRequestEvent input, Context context) {

        LOGGER.debug("Request event: " + input);
        LOGGER.debug("QSPs: " + input.getQueryStringParameters().toString());

        var odsCode = Optional.ofNullable(input.getQueryStringParameters().get(ODS_CODE_PARAM_KEY));
        var sessionId = input.getSessionId();
        var subjectClaim = input.getSubjectClaim();

        var missingItem = false;

        if (odsCode.isEmpty()) {
            missingItem = true;
            logMissingRequiredValue("ODS Code");
        }

        if (sessionId.isEmpty()) {
            missingItem = true;
            logMissingRequiredValue("sessionID");
        }

        if (subjectClaim.isEmpty()) {
            missingItem = true;
            logMissingRequiredValue("subjectClaim");
        }

        if (missingItem) {
            return orgHandlerError(HttpStatus.BAD_REQUEST.code);
        }

        try {
            sensitiveIndex.publish(
                    new VerifyOrgRequestAuditMessage(
                            sessionId.get().toString(), "Processing request to validate org"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var session = sessionStore.load(new Subject(subjectClaim.get()), sessionId.get());

        if (session.isEmpty()) {
            LOGGER.error("Unable to find Session using the provided SessionId and SubjectClaim");
            return orgHandlerError(HttpStatus.NOT_FOUND.code);
        }

        LOGGER.debug("Session found, processing match for ODS code {}", odsCode);

        var match =
                session.get().getOrganisations().stream()
                        .filter(org -> org.getOdsCode().equals(odsCode.get()))
                        .findFirst();

        if (match.isEmpty()) {
            LOGGER.error("ODS code did not match against user session");

            try {
                sensitiveIndex.publish(
                        new VerifyOrgRequestAuditMessage(
                                sessionId.get().toString(),
                                "Request with ODS code not matching user's permitted ODS codes"
                                        + " received"));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            return orgHandlerError(HttpStatus.BAD_REQUEST.code);
        }

        var userType = match.get().getOrgType();

        LOGGER.debug("Match found for user ODS code, user type set to: {}", userType);

        try {
            sensitiveIndex.publish(
                    new VerifyOrgRequestAuditMessage(
                            sessionId.get().toString(),
                            String.format(
                                    "Verify org request for ODS code %s successful, responding with"
                                            + " user type %s",
                                    odsCode, userType)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Origin", Utils.getAmplifyBaseUrl());

        var maxCookieAgeInSeconds =
                Duration.between(Instant.now(clock), session.get().getTimeToExist()).getSeconds();

        var userTypeCookie = httpOnlyCookieBuilder("UserType", userType, maxCookieAgeInSeconds);
        var cookies = List.of(userTypeCookie);
        var multiValueHeaders = Map.of("Set-Cookie", cookies);

        var response = new JSONObject();
        response.put("UserType", userType);

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withHeaders(headers)
                .withBody(response.toString())
                .withMultiValueHeaders(multiValueHeaders)
                .withStatusCode(HttpStatus.OK.code);
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

    private void logMissingRequiredValue(String missingValueName) {

        LOGGER.debug("{} is missing", missingValueName);

        try {
            sensitiveIndex.publish(new VerifyOrgBadRequestAuditMessage(missingValueName));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error publishing to Splunk, malformed JSON: {}", e.getMessage());
        }
    }
}
