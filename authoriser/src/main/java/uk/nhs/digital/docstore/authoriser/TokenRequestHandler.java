package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;
import java.net.MalformedURLException;
import java.time.Clock;
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
    public APIGatewayProxyResponseEvent handleRequest(TokenRequestEvent input, Context context) {
        var authCode = input.getAuthCode();
        if (authCode.isEmpty()) {
            throw new RuntimeException("Auth code is empty");
        }

        // possibly move to request event class
        if (!input.hasMatchingStateValues()) {
            var invalidStateResponse = new APIGatewayProxyResponseEvent();
            invalidStateResponse.setStatusCode(400);
            invalidStateResponse.setIsBase64Encoded(false);
            invalidStateResponse.setBody("");
            return invalidStateResponse;
        }
        Session session;
        try {
            session = OIDCClient.authoriseSession(authCode.get());
        } catch (AuthorisationException e) {
            throw new RuntimeException(e);
        }

        var headers = new HashMap<String, String>();
        headers.put(
                "Location", input.getRedirectUri().orElseThrow() + "?Role=" + session.getRole());

        // new secure cookie class with samesite, secure and httponly pre-set. implements tostring.
        var cookies =
                List.of(
                        cookieBuilder("State", input.getCookieState().orElseThrow().getValue(), 0L),
                        cookieBuilder(
                                "SubjectClaim",
                                session.getOIDCSubject(),
                                session.getTimeToExist() - Instant.now(clock).getEpochSecond()),
                        cookieBuilder(
                                "SessionId",
                                session.getId().toString(),
                                session.getTimeToExist() - Instant.now(clock).getEpochSecond()));
        var multiValueHeaders = Map.of("Set-Cookie", cookies);

        var response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);
        response.setBody("");
        response.setMultiValueHeaders(multiValueHeaders);

        return response;
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
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String cookieBuilder(String fieldName, String fieldContents, Long maxAge) {
        return fieldName
                + "="
                + fieldContents
                + "; SameSite=Strict; Secure; HttpOnly; Max-Age="
                + maxAge;
    }
}
