package uk.nhs.digital.docstore.authoriser.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.State;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.SessionManager;
import uk.nhs.digital.docstore.authoriser.enums.LoginEventOutcome;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

class TokenRequestHandlerTest {
    @Test
    void handleRequestRedirectsWithUserRoleWhenRequestStateIsValid() throws Exception {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        var state = new State();
        request.setQueryStringParameters(
                Map.of("code", authCode.getValue(), "state", state.getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + state.getValue()));
        var clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        var fixedTime = Instant.now(clock);
        var maxCookieAgeInSeconds = 100L;
        var cookieExpiryTime = fixedTime.plusSeconds(maxCookieAgeInSeconds);
        var session = new Session();
        session.setRole("Role");
        session.setOIDCSubject("subject");
        session.setTimeToExist(cookieExpiryTime);
        session.setId(UUID.randomUUID());
        session.setAccessTokenHash("AccesstokenHash");

        var loginOutcome = new LoginEventResponse(session, LoginEventOutcome.ONE_VALID_ORG);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager, clock);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        // assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getMultiValueHeaders().get("Set-Cookie"))
                .contains(
                        "State=" + state + "; SameSite=None; Secure; Path=/; Max-Age=0; HttpOnly");
        assertThat(response.getMultiValueHeaders().get("Set-Cookie"))
                .contains(
                        "SubjectClaim="
                                + session.getOIDCSubject()
                                + "; SameSite=None; Secure; Path=/; Max-Age="
                                + maxCookieAgeInSeconds
                                + "; HttpOnly");
        assertThat(response.getMultiValueHeaders().get("Set-Cookie"))
                .contains(
                        "SessionId="
                                + session.getId()
                                + "; SameSite=None; Secure; Path=/; Max-Age="
                                + maxCookieAgeInSeconds
                                + "; HttpOnly");
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenUserHasNoValidOrgs() throws Exception {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        var state = new State();
        request.setQueryStringParameters(
                Map.of("code", authCode.getValue(), "state", state.getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + state.getValue()));
        var clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        var fixedTime = Instant.now(clock);
        var maxCookieAgeInSeconds = 100L;
        var cookieExpiryTime = fixedTime.plusSeconds(maxCookieAgeInSeconds);
        var session = new Session();
        session.setRole("Role");
        session.setOIDCSubject("subject");
        session.setTimeToExist(cookieExpiryTime);
        session.setId(UUID.randomUUID());
        session.setAccessTokenHash("AccesstokenHash");

        var loginOutcome = new LoginEventResponse(session, LoginEventOutcome.NO_VALID_ORGS);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager, clock);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }

    @Test
    void handleRequestReturnsBadRequestUrlWhenTheRequestStateIsInvalid() throws Exception {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        request.setQueryStringParameters(
                Map.of("code", authCode.getValue(), "state", new State().getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + new State().getValue()));
        var session = new Session();
        session.setRole("some-role");

        var loginOutcome = new LoginEventResponse(session, LoginEventOutcome.ONE_VALID_ORG);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenTheStateCookieIsMissing() throws Exception {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        request.setQueryStringParameters(Map.of("code", authCode.getValue()));
        var session = new Session();
        session.setRole("some-role");

        var loginOutcome = new LoginEventResponse(session, LoginEventOutcome.ONE_VALID_ORG);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }
}
