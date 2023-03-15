package uk.nhs.digital.docstore.authoriser;

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
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requests.TokenRequestEvent;

public class TokenRequestHandler extends BaseAuthRequestHandler
        implements RequestHandler<TokenRequestEvent, APIGatewayProxyResponseEvent> {
    private final OIDCClient OIDCClient;

    private Clock clock = Clock.systemUTC();

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

        if (authCode.isEmpty()) {
            throw new RuntimeException("Auth code is empty");
        }

        if (!requestEvent.hasMatchingStateValues()) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withIsBase64Encoded(false)
                    .withBody("");
        }

        Session session;

        try {
            session = OIDCClient.authoriseSession(authCode.get());
        } catch (AuthorisationException exception) {
            throw new RuntimeException(exception);
        }

        var headers = new HashMap<String, String>();
        headers.put("Location", requestEvent.getRedirectUri().orElseThrow());

        // New secure cookie class with samesite, secure and httponly pre-set. Implements tostring
        var maxCookieAgeInSeconds =
                Duration.between(Instant.now(clock), session.getTimeToExist()).getSeconds();

        var stateCookie =
                httpOnlyCookieBuilder(
                        "State", requestEvent.getCookieState().orElseThrow().getValue(), 0L);
        var subjectClaimCookie =
                httpOnlyCookieBuilder(
                        "SubjectClaim", session.getOIDCSubject(), maxCookieAgeInSeconds);
        var sessionIdCookie =
                httpOnlyCookieBuilder(
                        "SessionId", session.getId().toString(), maxCookieAgeInSeconds);
        var loggedInCookie = cookieBuilder("LoggedIn", "True", maxCookieAgeInSeconds);

        var cookies = List.of(stateCookie, subjectClaimCookie, sessionIdCookie, loggedInCookie);
        var multiValueHeaders = Map.of("Set-Cookie", cookies);

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
