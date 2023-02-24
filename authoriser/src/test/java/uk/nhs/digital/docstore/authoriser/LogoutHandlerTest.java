package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LogoutHandlerTest {
    @Test
    public void removeExistingSessionIdFromSessionStore() {
        var request = new APIGatewayProxyRequestEvent();
        var sessionID = UUID.randomUUID();

        var handler = new LogoutHandler();
        request.setHeaders(Map.of(
                "Cookie",
                "SessionId=" + sessionID
        ));
        String redirectUrl = "some-url";
        var location = Map.of("redirect_uri", redirectUrl);
        request.setQueryStringParameters(location);

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertThat(response.getBody()).isEqualTo("");
        Assertions.assertThat(response.getIsBase64Encoded()).isFalse();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(redirectUrl);
        assertThat(response.getHeaders().get("Set-Cookie")).isEqualTo("SessionId=" + sessionID + "; Path=/; Max-Age=0");
    }
}