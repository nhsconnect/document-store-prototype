package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.net.MalformedURLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.*;
import uk.nhs.digital.docstore.authoriser.enums.LoginEventOutcome;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

public class TokenRequestHandler extends BaseAuthRequestHandler
        implements RequestHandler<TokenRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(TokenRequestHandler.class);
    public static final String AMPLIFY_BASE_URL_ENV_VAR = "AMPLIFY_BASE_URL";
    private final SessionManager sessionManager;

    private Clock clock = Clock.systemUTC();


    public static String getAmplifyBaseUrl() {
        String workspace = System.getenv("WORKSPACE");
        String url =
                (workspace == null || workspace.isEmpty())
                        ? "https://access-request-fulfilment.patient-deductions.nhs.uk/"
                        : String.format(
                                "https://%s.access-request-fulfilment.patient-deductions.nhs.uk/",
                                workspace);
        if (url == null) {
            LOGGER.warn("Missing required environment variable: " + AMPLIFY_BASE_URL_ENV_VAR);
            return "__unset__AMPLIFY_BASE_URL";
        }
        LOGGER.debug("get amplify base url:" + url);
        return url;
    }

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
                        new DynamoDBSessionStore(createDynamoDbMapper())));
    }

    public TokenRequestHandler(SessionManager sessionManager, Clock clock) {
        this.sessionManager = sessionManager;
        this.clock = clock;
    }

    public TokenRequestHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            TokenRequestEvent requestEvent, Context context) {

        var authCode = requestEvent.getAuthCode();

        LOGGER.debug("Handling token request");
        LOGGER.debug("Request event: " + requestEvent);

        if (authCode.isEmpty()) {
            LOGGER.debug("Auth code is empty");
            return authError("Auth code is empty");
        }

        if (!requestEvent.hasMatchingStateValues()) {
            LOGGER.debug(
                    "Mismatching state values. Cookie state: "
                            + requestEvent.getCookieState().orElse(null)
                            + " and query parameter state: "
                            + requestEvent.getQueryParameterState().orElse(null));

            return authError(
                    "Mismatching state values. Cookie state: "
                            + requestEvent.getCookieState().orElse(null)
                            + " and query parameter state: "
                            + requestEvent.getQueryParameterState().orElse(null));
        }

        LOGGER.debug(
                "Authorising session for state: " + requestEvent.getCookieState().orElse(null));

        LoginEventResponse loginResponse;

        try {
            loginResponse = sessionManager.createSession(authCode.get());
        } catch (Exception exception) {
            LOGGER.debug(exception.getMessage());
            return authError(exception.getMessage());
        }

        var session = loginResponse.getSession();

        if (loginResponse.getOutcome().equals(LoginEventOutcome.NO_VALID_ORGS)) {
            LOGGER.debug("user has no valid orgs to log in with");
            return authError("user has no valid orgs to log in with");
        }

        LOGGER.debug(
                "Successfully authorised session with state: "
                        + requestEvent.getCookieState().orElse(null));

        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Origin", getAmplifyBaseUrl());
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
        var role = "ADMIN";
        var roleCookie = httpOnlyCookieBuilder("RoleId", role, maxCookieAgeInSeconds);
        var cookies = List.of(stateCookie, subjectClaimCookie, sessionIdCookie, roleCookie);
        var multiValueHeaders = Map.of("Set-Cookie", cookies);

        LOGGER.debug(
                "Responding with auth cookies for session with ID ending in: "
                        + sessionId.substring(sessionId.length() - 4));
        var response = new JSONObject();
        response.put("SessionId", sessionIdCookie);
        response.put("RoleId", roleCookie);
        response.put("State", stateCookie);

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withHeaders(headers)
                .withBody(response.toString())
                .withMultiValueHeaders(multiValueHeaders);
    }

    private static APIGatewayProxyResponseEvent authError(String err) {
        var headers = new HashMap<String, String>();
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Access-Control-Allow-Origin", getAmplifyBaseUrl());
        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withStatusCode(ERROR_STATUS_CODE)
                .withHeaders(headers)
                .withBody(err);
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
