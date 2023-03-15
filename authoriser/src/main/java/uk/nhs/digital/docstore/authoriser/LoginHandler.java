package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler extends BaseAuthRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginHandler.class);

    private final AuthenticationRequestFactory authenticationRequestFactory;

    @SuppressWarnings("unused")
    public LoginHandler() {
        this(new AuthenticationRequestFactory(getClientInformation(), getProviderMetadata()));
    }

    public LoginHandler(AuthenticationRequestFactory authenticationRequestFactory) {
        this.authenticationRequestFactory = authenticationRequestFactory;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {
        var authRequest = authenticationRequestFactory.build();
        var authRequestUri = authRequest.toURI().toString();
        var authRequestState = authRequest.getState().getValue();
        var cookieState = httpOnlyCookieBuilder("State", authRequestState, 300L);

        LOGGER.debug("Redirecting user to " + authRequestUri);

        var headers = Map.of("Location", authRequestUri, "Set-Cookie", cookieState);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(SEE_OTHER_STATUS_CODE)
                .withHeaders(headers)
                .withIsBase64Encoded(false)
                .withBody("");
    }
}
