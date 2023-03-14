package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requests.LogoutRequestEvent;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

public class LogoutHandlerTest {
    @Test
    public void removeExistingSessionIdFromSessionStore() {
        var sessionStore = new InMemorySessionStore();
        var subject = new Subject("foo");
        var sessionID = UUID.randomUUID();
        var session =
                Session.create(sessionID, Instant.ofEpochSecond(1L), subject, new SessionID("sid"));
        sessionStore.save(session);

        var handler = new LogoutHandler(sessionStore);

        var request = new LogoutRequestEvent();
        request.setHeaders(
                Map.of("cookie", "SessionId=" + sessionID + ";SubjectClaim=" + subject.getValue()));

        String redirectUrl = "some-url";
        request.setQueryStringParameters(Map.of("redirect_uri", redirectUrl));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(redirectUrl);

        var setCookieHeaders = response.getMultiValueHeaders().get("Set-Cookie");
        assertThat(setCookieHeaders.get(0))
                .isEqualTo("SessionId=" + sessionID + "; Path=/; Max-Age=0");
        assertThat(setCookieHeaders.get(1))
                .isEqualTo("Subject=" + subject.getValue() + "; Path=/; Max-Age=0");

        assertThat(sessionStore.load(subject, session.getId())).isEmpty();
    }

    @Test
    public void removesNonExistingSessionIdFromSessionStoreDoesNothing() {
        var sessionStore = new InMemorySessionStore();
        var sessionID = UUID.randomUUID();
        var subject = new Subject("foo");

        var handler = new LogoutHandler(sessionStore);

        var request = new LogoutRequestEvent();
        request.setHeaders(
                Map.of("cookie", "SessionId=" + sessionID + ";SubjectClaim=" + subject.getValue()));

        String redirectUrl = "some-url";
        request.setQueryStringParameters(Map.of("redirect_uri", redirectUrl));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).isEqualTo(redirectUrl);

        var setCookieHeaders = response.getMultiValueHeaders().get("Set-Cookie");
        assertThat(setCookieHeaders.get(0))
                .isEqualTo("SessionId=" + sessionID + "; Path=/; Max-Age=0");
        assertThat(setCookieHeaders.get(1))
                .isEqualTo("Subject=" + subject.getValue() + "; Path=/; Max-Age=0");
    }

    @Test
    public void doesNothingIfSessionCookieDoesNotExist() {
        var sessionStore = new InMemorySessionStore();

        var handler = new LogoutHandler(sessionStore);

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
