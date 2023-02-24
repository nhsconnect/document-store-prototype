package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

import java.util.Map;
import java.util.UUID;

public class LogoutHandlerTest {
    @Test
    public void removeExistingSessionIdFromSessionStore(){
        var request = new APIGatewayProxyRequestEvent();
        var sessionID = UUID.randomUUID();
        LogoutHandler handler = new LogoutHandler();
        request.setHeaders(Map.of(
                "Cookie",
                "SessionId="+sessionID
        ));


        var response = handler.handleRequest(request, Mockito.mock(Context.class));


        Assertions.assertThat(response.getBody()).isEqualTo("");
        Assertions.assertThat(response.getIsBase64Encoded()).isFalse();
        Assertions.assertThat(response.getStatusCode()).isEqualTo(303);
    }
}