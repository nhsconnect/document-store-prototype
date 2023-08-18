package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.AuthenticationRequestFactory;
import uk.nhs.digital.docstore.authoriser.audit.message.StateAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.authoriser.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.authoriser.enums.HttpStatus;

public class LoginRedirectHandler extends BaseAuthRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoginRedirectHandler.class);

    private final AuthenticationRequestFactory authenticationRequestFactory;
    private final AuditPublisher sensitiveIndex;

    @SuppressWarnings("unused")
    public LoginRedirectHandler() {
        this(
                new AuthenticationRequestFactory(getClientInformation(), getProviderMetadata()),
                new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")));
    }

    public LoginRedirectHandler(
            AuthenticationRequestFactory authenticationRequestFactory,
            AuditPublisher sensitiveIndex) {
        this.authenticationRequestFactory = authenticationRequestFactory;
        this.sensitiveIndex = sensitiveIndex;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {

        var authRequest = authenticationRequestFactory.build();

        LOGGER.debug("Request event:" + authRequest);

        var authRequestUri = authRequest.toURI().toString();
        var authRequestState = authRequest.getState().getValue();
        var cookieState = httpOnlyCookieBuilder("State", authRequestState, 300L);
        var headers = Map.of("Location", authRequestUri, "Set-Cookie", cookieState);

        LOGGER.debug(
                "Redirecting user with state ending in: "
                        + authRequestState.substring(authRequestState.length() - 4)
                        + " to "
                        + authRequestUri);

        try {
            sensitiveIndex.publish(
                    new StateAuditMessage("Issuing new state cookie", authRequestState));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error publishing to Splunk, malformed JSON: {}", e.getMessage());
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatus.SEE_OTHER.code)
                .withHeaders(headers)
                .withIsBase64Encoded(false)
                .withBody("");
    }
}
