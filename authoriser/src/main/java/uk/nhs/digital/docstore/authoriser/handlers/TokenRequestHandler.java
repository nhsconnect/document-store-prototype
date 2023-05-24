package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.HTTPTokenRequestClient;
import uk.nhs.digital.docstore.authoriser.OIDCClient;
import uk.nhs.digital.docstore.authoriser.OIDCHttpClient;
import uk.nhs.digital.docstore.authoriser.OIDCTokenFetcher;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

public class TokenRequestHandler extends BaseAuthRequestHandler
        implements RequestHandler<TokenRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(TokenRequestHandler.class);
    public static final String AMPLIFY_BASE_URL_ENV_VAR = "AMPLIFY_BASE_URL";
    private final uk.nhs.digital.docstore.authoriser.OIDCClient OIDCClient;

    private Clock clock = Clock.systemUTC();

    public String getAmplifyBaseUrl() {
        String url = System.getenv(AMPLIFY_BASE_URL_ENV_VAR);
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
                new OIDCHttpClient(
                        new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())),
                        new OIDCTokenFetcher(
                                getClientInformation(),
                                new HTTPTokenRequestClient(),
                                getProviderMetadata()),
                        makeIDTokenValidator()));
    }

    public TokenRequestHandler(OIDCClient OIDCClient, Clock clock) {
        this.OIDCClient = OIDCClient;
        this.clock = clock;
    }

    public TokenRequestHandler(OIDCClient OIDCClient) {
        this.OIDCClient = OIDCClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            TokenRequestEvent requestEvent, Context context) {
        var authCode = requestEvent.getAuthCode();

        //         TODO: [PRMT-2779] Add identifier such as a redacted state
        LOGGER.debug("Handling token request");

        if (authCode.isEmpty()) {
            LOGGER.debug("Auth code is empty");
            var headers = new HashMap<String, String>();
            headers.put("Location", requestEvent.getErrorUri().orElseThrow());
            headers.put("Access-Control-Allow-Credentials", "true");
            return new APIGatewayProxyResponseEvent()
                    .withIsBase64Encoded(false)
                    .withStatusCode(SEE_OTHER_STATUS_CODE)
                    .withHeaders(headers)
                    .withBody("");
        }

        if (!requestEvent.hasMatchingStateValues()) {
            // TODO: [PRMT-2779] Add redaction if required
            LOGGER.debug(
                    "Mismatching state values. Cookie state: "
                            + requestEvent.getCookieState().orElse(null)
                            + " and query parameter state: "
                            + requestEvent.getQueryParameterState().orElse(null));

            var headers = new HashMap<String, String>();
            headers.put("Location", requestEvent.getErrorUri().orElseThrow());
            headers.put("Access-Control-Allow-Credentials", "true");
            return new APIGatewayProxyResponseEvent()
                    .withIsBase64Encoded(false)
                    .withStatusCode(SEE_OTHER_STATUS_CODE)
                    .withHeaders(headers)
                    .withBody("");
        }

        // TODO: [PRMT-2779] Add redaction if required
        LOGGER.debug(
                "Authorising session for state: " + requestEvent.getCookieState().orElse(null));

        Session session;

        try {
            session = OIDCClient.authoriseSession(authCode.get());
        } catch (AuthorisationException exception) {
            var headers = new HashMap<String, String>();
            headers.put("Location", requestEvent.getErrorUri().orElseThrow());
            headers.put("Access-Control-Allow-Credentials", "true");
            return new APIGatewayProxyResponseEvent()
                    .withIsBase64Encoded(false)
                    .withStatusCode(SEE_OTHER_STATUS_CODE)
                    .withHeaders(headers)
                    .withBody("");
        }

        // TODO: [PRMT-2779] Add redaction if required
        LOGGER.debug(
                "Successfully authorised session with state: "
                        + requestEvent.getCookieState().orElse(null));

        var headers = new HashMap<String, String>();
        headers.put("Location", requestEvent.getRedirectUri().orElseThrow());
        headers.put("Access-Control-Allow-Credentials", "true");
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

        // TODO: [PRMT-2779] Add or improve redaction if required
        LOGGER.debug(
                "Responding with auth cookies for session with ID ending in: "
                        + sessionId.substring(sessionId.length() - 4));

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withStatusCode(SEE_OTHER_STATUS_CODE)
                .withHeaders(headers)
                .withBody("")
                .withMultiValueHeaders(multiValueHeaders);
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
