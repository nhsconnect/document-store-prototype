package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import uk.nhs.digital.docstore.authoriser.requests.TokenRequestEvent;

public class TokenRequestHandler extends BaseAuthRequestHandler
        implements RequestHandler<TokenRequestEvent, APIGatewayProxyResponseEvent> {

    private final CIS2Client cis2Client;

    public TokenRequestHandler(CIS2Client cis2Client) {
        this.cis2Client = cis2Client;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(TokenRequestEvent input, Context context) {
        var authCode = input.getAuthCode();
        if (authCode.isEmpty()) {
            throw new RuntimeException("Auth code is empty");
        }

        var session = cis2Client.authoriseSession(authCode.get());

        if (session.isEmpty()) {
            throw new RuntimeException("No session returned by CIS2 client");
        }

        var headers = new HashMap<String, String>();
        headers.put(
                "Location",
                input.getRedirectUri().orElseThrow() + "?Role=" + session.get().getRole());

        var response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(SEE_OTHER_STATUS_CODE);
        response.setHeaders(headers);

        return response;
    }
}
