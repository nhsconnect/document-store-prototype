package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.Nonce;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthenticationRequestFactoryTest {
    @Test
    void buildReturnsConfiguredAuthenticationRequest() throws URISyntaxException {
        var serverURI = new URI("https://oidc.server.com/auth");
        var oidcClientConfig =
                new OIDCClientConfig(
                        Map.of(
                                "OIDC_CLIENT_ID", "foo",
                                "OIDC_AUTHORIZE_URL", serverURI.toString(),
                                "OIDC_CALLBACK_URL", "https://our-callback.url"));
        var factory = new AuthenticationRequestFactory(oidcClientConfig);

        var request = factory.build();
        assertThat(request.getResponseType()).isEqualTo(ResponseType.CODE);
        assertThat(request.getScope()).isEqualTo(new Scope("openid"));
        assertThat(request.getRedirectionURI())
                .isEqualTo(new URI(oidcClientConfig.getCallbackURL()));
        assertThat(request.getClientID()).isEqualTo(new ClientID(oidcClientConfig.getClientID()));
        assertThat(request.getState()).isInstanceOf(State.class);
        assertThat(request.getNonce()).isInstanceOf(Nonce.class);
        assertThat(request.toURI()).hasHost(serverURI.getHost());
    }
}
