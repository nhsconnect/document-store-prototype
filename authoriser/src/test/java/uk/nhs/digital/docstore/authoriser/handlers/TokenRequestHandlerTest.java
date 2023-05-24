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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.OIDCClient;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requestEvents.TokenRequestEvent;

class TokenRequestHandlerTest {
    @Test
    @Disabled
    void handleRequestRedirectsWithUserRoleWhenRequestStateIsValid() throws Exception {
        var request = new TokenRequestEvent();
        var redirectUrl = "some-url";
        var errorRedirectUrl = "some-error-url";
        var authCode = new AuthorizationCode();
        var state = new State();
        request.setQueryStringParameters(
                Map.of(
                        "redirect_uri",
                        redirectUrl,
                        "error_uri",
                        errorRedirectUrl,
                        "code",
                        authCode.getValue(),
                        "state",
                        state.getValue()));
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

        var oidcClient = Mockito.mock(OIDCClient.class);
        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);

        var handler = new TokenRequestHandler(oidcClient, clock);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
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
        assertThat(response.getHeaders().get("Location")).contains(redirectUrl);
    }

    @Test
    @Disabled
    void handleRequestReturnsBadRequestUrlWhenTheRequestStateIsInvalid() throws Exception {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        var errorRedirectUrl = "https://errorRedirect.uri";
        var redirectUrl = "https://redirect.uri";
        request.setQueryStringParameters(
                Map.of(
                        "redirect_uri",
                        redirectUrl,
                        "error_uri",
                        errorRedirectUrl,
                        "code",
                        authCode.getValue(),
                        "state",
                        new State().getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + new State().getValue()));
        var session = new Session();
        session.setRole("some-role");

        var oidcClient = Mockito.mock(OIDCClient.class);
        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);

        var handler = new TokenRequestHandler(oidcClient);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getHeaders().get("Location")).contains(errorRedirectUrl);
    }

    @Test
    @Disabled
    void handleRequestReturnsBadRequestResponseWhenTheStateCookieIsMissing() throws Exception {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();
        var errorRedirectUrl = "https://errorRedirect.uri";
        var redirectUrl = "https://redirect.uri";
        request.setQueryStringParameters(
                Map.of(
                        "redirect_uri",
                        redirectUrl,
                        "error_uri",
                        errorRedirectUrl,
                        "code",
                        authCode.getValue()));
        var session = new Session();
        session.setRole("some-role");

        var oidcClient = Mockito.mock(OIDCClient.class);
        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);

        var handler = new TokenRequestHandler(oidcClient);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getHeaders().get("Location")).contains(errorRedirectUrl);
    }
}
