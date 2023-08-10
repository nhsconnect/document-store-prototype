package uk.nhs.digital.docstore.authoriser.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.AuthenticationRequestFactory;
import uk.nhs.digital.docstore.authoriser.audit.message.StateAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.publisher.AuditPublisher;

public class LoginRedirectHandlerTest {

    private final AuditPublisher splunkPublisher = Mockito.mock(AuditPublisher.class);
    private final StateAuditMessage auditMessage = new StateAuditMessage("state");

    @Test
    public void returnsAnHttpRedirectToTheOIDCAuthorizeEndpoint()
            throws URISyntaxException, JsonProcessingException {
        var request = new APIGatewayProxyRequestEvent();
        var authenticationRequestFactory = Mockito.mock(AuthenticationRequestFactory.class);
        var authRequestBuilder =
                new AuthenticationRequest.Builder(
                        ResponseType.CODE,
                        new Scope("openid"),
                        new ClientID("foo"),
                        new URI("https://callback.url"));
        var state = new State();
        var authRequest =
                authRequestBuilder.state(state).endpointURI(new URI("https://oidc.server")).build();

        when(authenticationRequestFactory.build()).thenReturn(authRequest);
        Mockito.doNothing().when(splunkPublisher).publish(auditMessage);

        var handler = new LoginRedirectHandler(authenticationRequestFactory, splunkPublisher);
        var response = handler.handleRequest(request, mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(authRequest.toURI().toString());
        assertThat(response.getHeaders().get("Set-Cookie"))
                .isEqualTo(
                        "State="
                                + state.getValue()
                                + "; SameSite=None; Secure; Path=/; Max-Age=300; HttpOnly");
    }
}
