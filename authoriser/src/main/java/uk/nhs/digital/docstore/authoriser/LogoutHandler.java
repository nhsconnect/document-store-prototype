package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpCookie;
import java.util.Map;

public class LogoutHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LogoutHandler.class);
    public static final int SEE_OTHER_STATUS_CODE = 303;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        var cookies = HttpCookie.parse(input.getHeaders().get("Cookie"));
        var sessionIdCookie = cookies.stream().filter(httpCookie -> httpCookie.getName().equals("SessionId")).findFirst();

        if (sessionIdCookie.isEmpty()) {
            throw new RuntimeException("Handling of missing session cookie not yet implemented");
        }

        var sessionId = sessionIdCookie.get().getValue();
        var queryStringParameters = input.getQueryStringParameters();
        var headers = Map.of(
                "Location", queryStringParameters.get("redirect_uri"),
                "Set-Cookie", "SessionId=" + sessionId + "; Path=/; Max-Age=0"
        );

        var response = new APIGatewayProxyResponseEvent();

        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);
        response.setBody("");
        response.setIsBase64Encoded(false);

        return response;
    }
}

//Location header
//cookie
//delete the session id
