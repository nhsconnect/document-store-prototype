package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.State;
import uk.nhs.digital.docstore.authoriser.handlers.TokenRequestHandler;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

import java.util.Map;

public class Runner {
    public static void main(String[] args) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        var response = runTokenRequestHandler();
        System.out.println(
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    private static APIGatewayProxyResponseEvent runTokenRequestHandler() {
        var handler = new TokenRequestHandler();

        var request = new TokenRequestEvent();

        String redirectUrl = "some-url";
        var authCode = new AuthorizationCode("aLpVorJSZHJUz19Q91ZIrjPpJ9Y");
        var state = new State();
        request.setQueryStringParameters(
                Map.of(
                        "redirect_uri",
                        redirectUrl,
                        "code",
                        authCode.getValue(),
                        "state",
                        state.getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + state.getValue() + ""));

        return handler.handleRequest(request, null);
    }
}
