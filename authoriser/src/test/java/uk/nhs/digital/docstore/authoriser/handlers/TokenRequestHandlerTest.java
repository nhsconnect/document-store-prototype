package uk.nhs.digital.docstore.authoriser.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.State;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.authoriser.SessionManager;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

@ExtendWith(MockitoExtension.class)
class TokenRequestHandlerTest {

    TokenRequestEvent request = null;
    AuthorizationCode authCode = null;
    State state = null;
    Session session = null;
    Clock clock = null;
    TokenRequestHandler handler = null;

    final long maxCookieAgeInSeconds = 100L;
    @Mock SessionManager sessionManager;
    @Mock Context context;

    @BeforeEach
    public void setup() {
        request = new TokenRequestEvent();
        authCode = new AuthorizationCode();
        state = new State();
        session = new Session();
        clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);

        request.setQueryStringParameters(
                Map.of("code", authCode.getValue(), "state", state.getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + state.getValue()));

        var fixedTime = Instant.now(clock);
        var cookieExpiryTime = fixedTime.plusSeconds(maxCookieAgeInSeconds);
        var session = new Session();
        session.setRole("Role");
        session.setOIDCSubject("subject");
        session.setTimeToExist(cookieExpiryTime);
        session.setId(UUID.randomUUID());
        session.setAccessTokenHash("AccesstokenHash");

        handler = new TokenRequestHandler(sessionManager, clock);
    }

    @AfterEach
    public void tearDown() {
        request = null;
        authCode = null;
        state = null;
        session = null;
        clock = null;
        handler = null;
    }

    @Test
    void handleRequestReturnsCookiesAndOrgForSingleValidOrgUser() throws LoginException {
        var org = List.of(new Organisation("ODS", "Name", PermittedOrgs.GPP.type));
        var expectedJsonBody = new JSONObject().put("Organisations", org);

        var loginOutcome = new LoginEventResponse(session, org);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var response = handler.handleRequest(request, context);

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

        assertTrue(expectedJsonBody.similar(new JSONObject(response.getBody())));
    }

    @Test
    public void handleRequestReturnsCookiesAndOrgsForSingleValidOrgUser() throws LoginException {
        var orgs =
                List.of(
                        new Organisation("A100", "Town GP", PermittedOrgs.GPP.type),
                        new Organisation("A142", "City clinic", PermittedOrgs.DEV.type),
                        new Organisation("A410", "National care support", PermittedOrgs.PCSE.type));
        var expectedJsonBody = new JSONObject();
        expectedJsonBody.put("Organisations", orgs);

        var loginOutcome = new LoginEventResponse(session, orgs);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var response = handler.handleRequest(request, context);

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

        assertTrue(expectedJsonBody.similar(new JSONObject(response.getBody())));
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenUserHasNoValidOrgs() throws LoginException {
        List<Organisation> orgs = List.of();

        var loginOutcome = new LoginEventResponse(session, orgs);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var response = handler.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getBody()).isEmpty();
        assertThat(response.getIsBase64Encoded()).isFalse();
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenTheRequestStateIsInvalid()
            throws LoginException {
        session.setRole("some-role");

        List<Organisation> orgs = List.of();

        var loginOutcome = new LoginEventResponse(session, orgs);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var response = handler.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEmpty();
        assertThat(response.getIsBase64Encoded()).isFalse();
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenTheStateCookieIsMissing() throws LoginException {
        request.setQueryStringParameters(Map.of("code", authCode.getValue()));
        session.setRole("some-role");

        List<Organisation> orgs = List.of();

        var loginOutcome = new LoginEventResponse(session, orgs);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager);
        var response = handler.handleRequest(request, context);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }
}
