package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.util.Map;
import java.util.Optional;

import com.nimbusds.oauth2.sdk.id.State;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requests.TokenRequestEvent;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class TokenRequestHandlerTest {

    @Test
    void handleRequestRedirectsWithUserRoleWhenRequestStateIsValid() {
        //        Check valid session exists in cache
        //        Request Token frmo cis2
        //        take Access and ID tokens from cis2
        //        Update cache with user info
        //        Redirect browser with User Roles cookie
        var request = new TokenRequestEvent();
        String redirectUrl = "some-url";
        var authCode = new AuthorizationCode();
        var state = new State();
        request.setQueryStringParameters(
                Map.of("redirect_uri", redirectUrl, "code", authCode.getValue(), "state", state.getValue()));

        var session = new Session();
        session.setRole("Role");
        session.setAuthStateParameter(state);
        var cis2Client = Mockito.mock(CIS2Client.class);
        Mockito.when(cis2Client.authoriseSession(authCode)).thenReturn(Optional.of(session));

        var sessionStore = new InMemorySessionStore();
        sessionStore.save(session);

        var handler = new TokenRequestHandler(cis2Client, sessionStore);

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(303);
        assertThat(response.getHeaders().get("Location")).startsWith(redirectUrl);
        assertThat(response.getHeaders().get("Location")).contains("Role=Role");
        assertThat(response.getIsBase64Encoded()).isFalse();
    }

    @Test
    void handleRequestReturnsBadRequestResponseWhenTheRequestStateIsInvalid() {
        var request = new TokenRequestEvent();
        var authCode = new AuthorizationCode();

        request.setQueryStringParameters(Map.of(
                "redirect_uri", "https://redirect.uri",
                "code", authCode.getValue(),
                "state", new State().getValue()
        ));

        var session = new Session();
        session.setRole("some-role");

        var sessionStore = new InMemorySessionStore();
        var cis2Client = Mockito.mock(CIS2Client.class);

        Mockito.when(cis2Client.authoriseSession(authCode)).thenReturn(Optional.of(session));
        var handler = new TokenRequestHandler(cis2Client, sessionStore);

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(400);
    }
}
