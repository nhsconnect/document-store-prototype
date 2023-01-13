package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthoriserTest {

    @org.junit.jupiter.api.Test
    void handleRequest() {
        var event = new APIGatewayProxyRequestEvent();
        var handler = new Authoriser();

        String token = "token";
        event.setHeaders(Map.of("Authorization", token));

        var response = handler.handleRequest(event, null);

        assertThat(response.getContext().get("token")).isEqualTo(token);
    }
}