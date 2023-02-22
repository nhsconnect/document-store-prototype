package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LoginHandlerTest {
    @Test
    public void returnsAnHttpRedirectToTheOIDCAuthorizeEndpoint() throws URISyntaxException {
        var request = new APIGatewayProxyRequestEvent();
        var uuid = UUID.randomUUID();
        var authenticationRequestFactory = Mockito.mock(AuthenticationRequestFactory.class);
        var uuidProvider = Mockito.mock(UUIDProvider.class);

        var authRequestBuilder =
                new AuthenticationRequest.Builder(
                        ResponseType.CODE,
                        new Scope("openid"),
                        new ClientID("foo"),
                        new URI("https://callback.url"));
        authRequestBuilder.endpointURI(new URI("https://oidc.server"));
        var authRequest = authRequestBuilder.build();
        Mockito.when(authenticationRequestFactory.build()).thenReturn(authRequest);
        Mockito.when(uuidProvider.generateUUID()).thenReturn(uuid);

        var handler = new LoginHandler(authenticationRequestFactory, uuidProvider);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(authRequest.toURI().toString());
        assertThat(response.getHeaders().get("Set-Cookie"))
                .isEqualTo("SessionId=" + uuid + "; SameSite=Strict; Secure; HttpOnly");
    }
}
