package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LoginHandlerTest {
    @Test
    public void returnsAnHttpRedirectToTheOIDCAuthorizeEndpoint() throws URISyntaxException {
        var request = new APIGatewayProxyRequestEvent();
        var authenticationRequestFactory = Mockito.mock(AuthenticationRequestFactory.class);

        var authRequestBuilder =
                new AuthenticationRequest.Builder(
                        ResponseType.CODE,
                        new Scope("openid"),
                        new ClientID("foo"),
                        new URI("https://callback.url"));
        authRequestBuilder.endpointURI(new URI("https://oidc.server"));
        var state = new State();
        authRequestBuilder.state(state);
        var authRequest = authRequestBuilder.build();

        Mockito.when(authenticationRequestFactory.build()).thenReturn(authRequest);

        var handler = new LoginHandler(authenticationRequestFactory);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(authRequest.toURI().toString());
        assertThat(response.getHeaders().get("Set-Cookie"))
                .isEqualTo("State=" + state.getValue() + "; SameSite=Strict; Secure; HttpOnly");
    }
}
