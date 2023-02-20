package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class AuthenticationRequestFactoryTest {
    @Test
    void buildReturnsConfiguredAuthenticationRequest() throws URISyntaxException {
        var metadata = new OIDCClientMetadata();
        var serverURI = new URI("https://oidc.server.com/auth");
        metadata.setInitiateLoginURI(serverURI);
        var factory = new AuthenticationRequestFactory(metadata);

        var request = factory.build();
        assertThat(request.getResponseType()).isEqualTo(ResponseType.CODE);
        assertThat(request.getScope()).isEqualTo(new Scope("openid"));
        assertThat(request.getRedirectionURI()).isEqualTo(new URI("https://our-callback.url"));
        assertThat(request.getClientID()).isEqualTo(new ClientID("foo"));
        assertThat(request.getState()).isInstanceOf(State.class);
        assertThat(request.getNonce()).isInstanceOf(Nonce.class);
        assertThat(request.toURI()).hasHost(serverURI.getHost());
    }
}
