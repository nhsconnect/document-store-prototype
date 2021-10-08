package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloWorldTest {
    @Test
    void returnsHelloWorld() {
        var handler = new HelloWorldHandler();
        var event = new APIGatewayProxyRequestEvent();

        var response = handler.handleRequest(event, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Hello World!");
    }
}
