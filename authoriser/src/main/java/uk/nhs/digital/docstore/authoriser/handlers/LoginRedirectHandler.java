package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.AuthenticationRequestFactory;

public class LoginRedirectHandler extends BaseAuthRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginRedirectHandler.class);

    private final AuthenticationRequestFactory authenticationRequestFactory;

    @SuppressWarnings("unused")
    public LoginRedirectHandler() {
        this(new AuthenticationRequestFactory(getClientInformation(), getProviderMetadata()));
    }

    public LoginRedirectHandler(AuthenticationRequestFactory authenticationRequestFactory) {
        this.authenticationRequestFactory = authenticationRequestFactory;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {
        // Todo: Check user organisation / role to send on correct user journey

        var authRequest = authenticationRequestFactory.build();
        var resources = authRequest.getResources();

        LOGGER.debug("///// REQUEST EVENT LOGG ////" + authRequest);
        LOGGER.debug("///// RESOURCES EVENT LOGG ////" + resources);

        var authRequestUri = authRequest.toURI().toString();
        var authRequestState = authRequest.getState().getValue();
        var cookieState = httpOnlyCookieBuilder("State", authRequestState, 300L);
        var headers = Map.of("Location", authRequestUri, "Set-Cookie", cookieState);

        // TODO: [PRMT-2779] Add identifier such as redacted state
        LOGGER.debug(
                "Redirecting user with state ending in: "
                        + authRequestState.substring(authRequestState.length() - 4)
                        + " to "
                        + authRequestUri);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(SEE_OTHER_STATUS_CODE)
                .withHeaders(headers)
                .withIsBase64Encoded(false)
                .withBody("");
    }
}
