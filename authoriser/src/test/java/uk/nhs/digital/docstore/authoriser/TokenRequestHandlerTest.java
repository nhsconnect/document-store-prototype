package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.State;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requests.TokenRequestEvent;

class TokenRequestHandlerTest {

    @Test
    void handleRequestRedirectsWithUserRoleWhenRequestStateIsValid() throws Exception {
        //        Check valid session exists in cache
        //        Request Token from cis2
        //        take Access and ID tokens from cis2
        //        Update cache with user info
        //        Redirect browser with User Roles cookie
        var request = new TokenRequestEvent();

        String redirectUrl = "some-url";
        var authCode = new AuthorizationCode();
        var state = new State();
        request.setQueryStringParameters(
                Map.of(
                        "redirect_uri",
                        redirectUrl,
                        "code",
                        authCode.getValue(),
                        "state",
                        state.getValue()));
        request.setHeaders(Map.of("Cookie", "State=" + state.getValue() + ""));

        var session = new Session();
        session.setRole("Role");

        var cis2Client = Mockito.mock(CIS2Client.class);

        Mockito.when(cis2Client.authoriseSession(authCode)).thenReturn(session);

        var handler = new TokenRequestHandler(cis2Client);

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).startsWith(redirectUrl);
        assertThat(response.getHeaders().get("Location")).contains("Role=Role");
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getHeaders().get("Set-Cookie"))
                .isEqualTo("State=" + state + "; SameSite=Strict; Secure; HttpOnly; Max-Age=0");
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenTheRequestStateIsInvalid() throws Exception {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();

        request.setQueryStringParameters(
                Map.of(
                        "redirect_uri", "https://redirect.uri",
                        "code", authCode.getValue(),
                        "state", new State().getValue()));
        request.setHeaders(
                Map.of("Cookie", "State=" + new State().getValue() + "; Secure; HttpOnly"));

        var session = new Session();
        session.setRole("some-role");

        var cis2Client = Mockito.mock(CIS2Client.class);

        Mockito.when(cis2Client.authoriseSession(authCode)).thenReturn(session);
        var handler = new TokenRequestHandler(cis2Client);

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }
}
