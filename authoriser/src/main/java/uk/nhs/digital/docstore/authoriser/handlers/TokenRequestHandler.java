package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.net.MalformedURLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.*;
import uk.nhs.digital.docstore.authoriser.Utils;
import uk.nhs.digital.docstore.authoriser.apiRequestClients.OIDCHttpClient;
import uk.nhs.digital.docstore.authoriser.apiRequestClients.OIDCTokenFetcher;
import uk.nhs.digital.docstore.authoriser.apiRequestClients.UserInfoFetcher;
import uk.nhs.digital.docstore.authoriser.audit.message.StateAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.authoriser.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.authoriser.config.Tracer;
import uk.nhs.digital.docstore.authoriser.enums.HttpStatus;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

public class TokenRequestHandler extends BaseAuthRequestHandler
        implements RequestHandler<TokenRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(TokenRequestHandler.class);
    private final SessionManager sessionManager;
    private Clock clock = Clock.systemUTC();

    private final AuditPublisher sensitiveIndex;

    @SuppressWarnings("unused")
    public TokenRequestHandler() {
        this(
                new SessionManager(
                        new OIDCHttpClient(
                                new OIDCTokenFetcher(
                                        getClientInformation(),
                                        new HTTPTokenRequestClient(),
                                        getProviderMetadata()),
                                new UserInfoFetcher(
                                        new HTTPUserInfoRequestClient(), getProviderMetadata()),
                                makeIDTokenValidator()),
                        new DynamoDBSessionStore(createDynamoDbMapper())),
                new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")));
    }

    public TokenRequestHandler(
            SessionManager sessionManager, Clock clock, AuditPublisher sensitiveIndex) {
        this(sessionManager, sensitiveIndex);
        this.clock = clock;
    }

    public TokenRequestHandler(SessionManager sessionManager, AuditPublisher sensitiveIndex) {
        this.sessionManager = sessionManager;
        this.sensitiveIndex = sensitiveIndex;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            TokenRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        var authCode = requestEvent.getAuthCode();

        LOGGER.debug("Handling token request");
        LOGGER.debug("Request event: " + requestEvent);

        if (authCode.isEmpty()) {
            LOGGER.debug("Auth code is empty");
            return authError(HttpStatus.BAD_REQUEST.code);
        }

        if (!requestEvent.hasMatchingStateValues()) {
            LOGGER.debug(
                    "Mismatching state values. Cookie state: "
                            + requestEvent.getCookieState().orElse(null)
                            + " and query parameter state: "
                            + requestEvent.getQueryParameterState().orElse(null));

            return authError(HttpStatus.BAD_REQUEST.code);
        }

        logState(requestEvent.getCookieState().orElse(null).toString());

        LoginEventResponse loginResponse;

        try {
            loginResponse = sessionManager.createSession(authCode.get());
        } catch (LoginException exception) {
            LOGGER.debug(exception.getMessage());
            return authError(HttpStatus.FORBIDDEN.code);
        }

        var session = loginResponse.getSession();

        if (loginResponse.getUsersOrgs().isEmpty()) {
            LOGGER.debug("user has no valid organisations to log in");
            return authError(HttpStatus.UNAUTHORISED.code);
        }

        LOGGER.debug(
                "Successfully authorised session with state: "
                        + requestEvent.getCookieState().orElse(null));

        var response = new JSONObject();
        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Origin", Utils.getAmplifyBaseUrl());
        var maxCookieAgeInSeconds =
                Duration.between(Instant.now(clock), session.getTimeToExist()).getSeconds();
        var sessionId = session.getId().toString();

        var stateCookie =
                httpOnlyCookieBuilder(
                        "State", requestEvent.getCookieState().orElseThrow().getValue(), 0L);
        var subjectClaimCookie =
                httpOnlyCookieBuilder(
                        "SubjectClaim", session.getOIDCSubject(), maxCookieAgeInSeconds);
        var sessionIdCookie = httpOnlyCookieBuilder("SessionId", sessionId, maxCookieAgeInSeconds);
        var cookies = List.of(stateCookie, subjectClaimCookie, sessionIdCookie);
        var multiValueHeaders = Map.of("Set-Cookie", cookies);

        LOGGER.debug(
                "Responding with auth cookies for session with ID ending in: "
                        + sessionId.substring(sessionId.length() - 4));

        response.put("Organisations", loginResponse.getUsersOrgs());

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withHeaders(headers)
                .withBody(response.toString())
                .withMultiValueHeaders(multiValueHeaders);
    }

    private void logState(String state) {

        state = (state == null) ? "null" : state;

        var messageDescription = "Authorising session for state";

        LOGGER.debug(messageDescription + ": " + state);

        try {
            sensitiveIndex.publish(new StateAuditMessage(messageDescription, state));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error publishing to Splunk, malformed JSON: {}", e.getMessage());
        }
    }

    private static APIGatewayProxyResponseEvent authError(int statusCode) {
        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Origin", Utils.getAmplifyBaseUrl());
        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody("");
    }

    private static IDTokenValidator makeIDTokenValidator() {
        var clientInfo = getClientInformation();
        var providerMetadata = getProviderMetadata();

        try {
            return new IDTokenValidator(
                    providerMetadata.getIssuer(),
                    clientInfo.getID(),
                    JWSAlgorithm.RS256,
                    providerMetadata.getJWKSetURI().toURL());
        } catch (MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
