package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.id.State;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requests.LogoutRequestEvent;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

public class LogoutHandlerTest {
    private static OIDCClientConfig config;

    @BeforeAll
    public static void setup() {
        var environmentVars = Map.of("AUTH_FAILURE_REDIRECT_URI", "http/redirect");
        config = new OIDCClientConfig(environmentVars);
    }

    @Test
    public void removeExistingSessionIdFromSessionStore() {
        var sessionStore = new InMemorySessionStore();
        var sessionID = UUID.randomUUID();
        var session = Session.create(sessionID, 1L, new State());
        sessionStore.save(session);

        var handler = new LogoutHandler(sessionStore, config);

        var request = new LogoutRequestEvent();
        request.setHeaders(Map.of("Cookie", "SessionId=" + sessionID));

        String redirectUrl = "some-url";
        request.setQueryStringParameters(Map.of("redirect_uri", redirectUrl));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(redirectUrl);
        assertThat(response.getHeaders().get("Set-Cookie"))
                .isEqualTo("SessionId=" + sessionID + "; Path=/; Max-Age=0");

        assertThat(sessionStore.load(session.getId())).isEmpty();
    }

    @Test
    public void returnsBadRequestErrorWhenRedirectUriParameterIsMissing() {
        var sessionStore = new InMemorySessionStore();
        var sessionID = UUID.randomUUID();
        var session = Session.create(sessionID, 1L, new State());
        sessionStore.save(session);

        var handler = new LogoutHandler(sessionStore, config);

        var request = new LogoutRequestEvent();
        request.setHeaders(Map.of("Cookie", "SessionId=" + sessionID));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getBody()).isEmpty();
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location"))
                .startsWith(config.getAuthFailureRedirectUri());
        assertThat(response.getHeaders().get("Location"))
                .endsWith("?error=missing_parameter&error_description=missing_redirect_URI");
    }

    @Test
    public void removesNonExistingSessionIdFromSessionStoreDoesNothing() {
        var sessionStore = new InMemorySessionStore();
        var sessionID = UUID.randomUUID();

        var handler = new LogoutHandler(sessionStore, config);

        var request = new LogoutRequestEvent();
        request.setHeaders(Map.of("Cookie", "SessionId=" + sessionID));

        String redirectUrl = "some-url";
        request.setQueryStringParameters(Map.of("redirect_uri", redirectUrl));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(redirectUrl);
        assertThat(response.getHeaders().get("Set-Cookie"))
                .isEqualTo("SessionId=" + sessionID + "; Path=/; Max-Age=0");
    }

    @Test
    public void doesNothingIfSessionCookieDoesNotExist() {
        var sessionStore = new InMemorySessionStore();

        var handler = new LogoutHandler(sessionStore, config);

        var request = new LogoutRequestEvent();

        String redirectUrl = "some-url";
        request.setQueryStringParameters(Map.of("redirect_uri", redirectUrl));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(redirectUrl);
    }
}
