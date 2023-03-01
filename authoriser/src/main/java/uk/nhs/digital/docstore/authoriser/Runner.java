package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Runner {
    public static void main(String[] args) throws JsonProcessingException {
        var objectMapper = new ObjectMapper();
        var response = runLoginHandler();
        System.out.println(
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }

    private static APIGatewayProxyResponseEvent runLoginHandler() {
        var handler = new LoginHandler();
        return handler.handleRequest(new APIGatewayProxyRequestEvent(), null);
    }
}
