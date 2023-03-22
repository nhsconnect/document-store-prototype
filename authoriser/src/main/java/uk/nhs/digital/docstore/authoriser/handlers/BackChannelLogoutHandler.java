package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.util.URLUtils;
import com.nimbusds.openid.connect.sdk.claims.LogoutTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;
import java.text.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;

@SuppressWarnings("unused")
public class BackChannelLogoutHandler extends BaseAuthRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final SessionStore sessionStore;
    private final LogoutTokenValidator tokenValidator;
    public static final Logger LOGGER = LoggerFactory.getLogger(BackChannelLogoutHandler.class);

    public BackChannelLogoutHandler() {
        this(
                getLogoutTokenValidator(),
                new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())));
    }

    public BackChannelLogoutHandler(
            LogoutTokenValidator tokenValidator, SessionStore sessionStore) {
        this.sessionStore = sessionStore;
        this.tokenValidator = tokenValidator;
    }

    private static LogoutTokenValidator getLogoutTokenValidator() {
        var providerMetadata = getProviderMetadata();
        return new LogoutTokenValidator(
                providerMetadata.getIssuer(),
                getClientInformation().getID(),
                JWSAlgorithm.RS256,
                providerMetadata.getJWKSet());
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {
        var parameters = URLUtils.parseParameters(requestEvent.getBody());
        var rawLogoutToken = parameters.get("logout_token").get(0);

        JWT logoutToken;

        try {
            logoutToken = JWTParser.parse(rawLogoutToken);
        } catch (ParseException exception) {
            throw new RuntimeException(exception);
        }

        LogoutTokenClaimsSet claims;

        try {
            claims = tokenValidator.validate(logoutToken);
        } catch (BadJOSEException | JOSEException exception) {
            LOGGER.debug("Invalid token " + logoutToken);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("")
                    .withIsBase64Encoded(false);
        }

        var sessions = sessionStore.queryByOIDCSubject(claims.getSubject());

        sessionStore.batchDelete(sessions);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withIsBase64Encoded(false)
                .withBody("");
    }
}
