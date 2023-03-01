package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import uk.nhs.digital.docstore.authoriser.requests.TokenRequestEvent;

public class TokenRequestHandler extends BaseAuthRequestHandler
        implements RequestHandler<TokenRequestEvent, APIGatewayProxyResponseEvent> {

    private final CIS2Client cis2Client;

    @SuppressWarnings("unused")
    public TokenRequestHandler() {
        this(
                new CIS2HttpClient(
                        new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())),
                        new OIDCTokenFetcher()));
    }

    public TokenRequestHandler(CIS2Client cis2Client) {
        this.cis2Client = cis2Client;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(TokenRequestEvent input, Context context) {
        var authCode = input.getAuthCode();
        if (authCode.isEmpty()) {
            throw new RuntimeException("Auth code is empty");
        }
        var queryParameterState = input.getQueryParameterState();
        var cookieState = input.getCookieState();
        if (queryParameterState.isEmpty()
                || cookieState.isEmpty()
                || !queryParameterState.get().equals(cookieState.get())) {
            var invalidStateResponse = new APIGatewayProxyResponseEvent();
            invalidStateResponse.setStatusCode(400);
            invalidStateResponse.setIsBase64Encoded(false);
            invalidStateResponse.setBody("");
            return invalidStateResponse;
        }
        var session = cis2Client.authoriseSession(authCode.get());

        if (session.isEmpty()) {
            throw new RuntimeException("No session returned by CIS2 client");
        }

        var headers = new HashMap<String, String>();
        headers.put(
                "Location",
                input.getRedirectUri().orElseThrow() + "?Role=" + session.get().getRole());
        headers.put(
                "Set-Cookie",
                "State="
                        + input.getCookieState().orElseThrow()
                        + "; SameSite=Strict; Secure; HttpOnly; Max-Age=0");

        var response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);

        return response;
    }
}
