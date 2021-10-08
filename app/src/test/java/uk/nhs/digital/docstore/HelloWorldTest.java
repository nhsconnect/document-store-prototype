package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloWorldTest {
    @Test
    void returnsHelloWorld() {
        var handler = new HelloWorldHandler();
        var event = new APIGatewayV2HTTPEvent();

        var response = handler.handleRequest(event, null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Hello World!");
    }
}
