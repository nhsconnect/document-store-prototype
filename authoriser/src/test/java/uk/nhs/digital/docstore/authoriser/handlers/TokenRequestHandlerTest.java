package uk.nhs.digital.docstore.authoriser.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.State;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.SessionManager;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

class TokenRequestHandlerTest {

    @Test
    void handleRequestRedirectsForSingleValidOrgUser() throws LoginException {
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

        HashMap<String, List<String>> usersOrgs = new HashMap<>();
        usersOrgs.put("odsCode", List.of("R076"));

        var orgs = List.of(new Organisation("ODS", "Name", PermittedOrgs.GPP.type));

        var loginOutcome = new LoginEventResponse(session, orgs);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager, clock);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

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
        // assertThat(response.getBody().contains())
    }

    @Test
    public void handleRequestRedirectsWithUserRoleWhenMultipleRolesAreFound()
            throws LoginException {
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

        var orgs =
                List.of(
                        new Organisation("A100", "Town GP", PermittedOrgs.GPP.type),
                        new Organisation("A142", "City clinic", PermittedOrgs.DEV.type),
                        new Organisation("A410", "National care support", PermittedOrgs.PCSE.type));

        var loginOutcome = new LoginEventResponse(session, orgs);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager, clock);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

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
    void handleRequestReturnsBadRequestResponseWhenUserHasNoValidOrgs() throws LoginException {
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

        List<Organisation> orgs = List.of();

        var loginOutcome = new LoginEventResponse(session, orgs);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager, clock);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(401);
        //        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenTheRequestStateIsInvalid()
            throws LoginException {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        request.setQueryStringParameters(
                Map.of("code", authCode.getValue(), "state", new State().getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + new State().getValue()));
        var session = new Session();
        session.setRole("some-role");

        List<Organisation> orgs = List.of();

        var loginOutcome = new LoginEventResponse(session, orgs);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenTheStateCookieIsMissing() throws LoginException {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        request.setQueryStringParameters(Map.of("code", authCode.getValue()));
        var session = new Session();
        session.setRole("some-role");

        List<Organisation> orgs = List.of();

        var loginOutcome = new LoginEventResponse(session, orgs);
        var sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(sessionManager.createSession(authCode)).thenReturn(loginOutcome);

        var handler = new TokenRequestHandler(sessionManager);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getIsBase64Encoded()).isFalse();
    }
}
