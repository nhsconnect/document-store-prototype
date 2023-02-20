package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.Map;

public class LoginHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final int SEE_OTHER_STATUS_CODE = 303;
    private final AuthenticationRequestFactory authenticationRequestFactory;

    public LoginHandler(AuthenticationRequestFactory authenticationRequestFactory) {
        this.authenticationRequestFactory = authenticationRequestFactory;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {
        var response = new APIGatewayProxyResponseEvent();

        var authRequest = authenticationRequestFactory.build();

        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(Map.of("Location", authRequest.toURI().toString()));

        return response;
    }
}
