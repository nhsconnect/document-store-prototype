package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpCookie;
import java.util.Map;
import java.util.UUID;

public class LogoutHandler extends BaseAuthRequestHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    public static final Logger LOGGER = LoggerFactory.getLogger(LogoutHandler.class);

    private final SessionStore sessionStore;

    public LogoutHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public LogoutHandler() {
        this(new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent input, Context context) {

        var cookies = HttpCookie.parse(input.getHeaders().get("Cookie"));
        var sessionIdCookie = cookies.stream().filter(httpCookie -> httpCookie.getName().equals("SessionId")).findFirst();

        if (sessionIdCookie.isEmpty()) {
            throw new RuntimeException("Handling of missing session cookie not yet implemented");
        }

        var sessionId = UUID.fromString(sessionIdCookie.get().getValue());

        sessionStore.delete(sessionId);

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

//delete the session id
