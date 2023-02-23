package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

public class LoginHandlerTest {
    @Test
    public void returnsAnHttpRedirectToTheOIDCAuthorizeEndpoint() throws URISyntaxException {
        var request = new APIGatewayProxyRequestEvent();
        var sessionID = UUID.randomUUID();
        var authenticationRequestFactory = Mockito.mock(AuthenticationRequestFactory.class);
        var uuidProvider = Mockito.mock(UUIDProvider.class);
        var clock = Clock.fixed(Instant.now(), TimeZone.getDefault().toZoneId());

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
        Mockito.when(uuidProvider.generateUUID()).thenReturn(sessionID);

        var sessionStore = new InMemorySessionStore();
        var handler =
                new LoginHandler(authenticationRequestFactory, sessionStore, uuidProvider, clock);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(authRequest.toURI().toString());
        assertThat(response.getHeaders().get("Set-Cookie"))
                .isEqualTo("SessionId=" + sessionID + "; SameSite=Strict; Secure; HttpOnly");

        var session = sessionStore.load(sessionID.toString());
        assertThat(session.isPresent()).isTrue();
        assertThat(session.get().getId()).isEqualTo(sessionID.toString());
        assertThat(session.get().getAuthStateParameter())
                .isEqualTo(authRequest.getState().toString());
        var timeToExit = Instant.now(clock).plus(1, ChronoUnit.HOURS).getEpochSecond();
        assertThat(session.get().getTimeToExist()).isEqualTo(BigInteger.valueOf(timeToExit));
    }
}
