package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuthenticationRequestFactoryTest {
    @Test
    void buildReturnsConfiguredAuthenticationRequest() throws URISyntaxException {
        var serverURI = new URI("https://oidc.server.com/auth");
        var clientMetadata = new OIDCClientMetadata();
        clientMetadata.setRedirectionURI(new URI("https://our-callback.url"));
        var clientInformation =
                new OIDCClientInformation(
                        new ClientID("foo"), null, clientMetadata, new Secret("bar"));
        var providerMetadata =
                new OIDCProviderMetadata(
                        new Issuer("test"),
                        List.of(SubjectType.PUBLIC),
                        new URI("http://jwks.uri"));
        providerMetadata.setAuthorizationEndpointURI(serverURI);
        providerMetadata.setScopes(new Scope("openid"));
        var factory = new AuthenticationRequestFactory(clientInformation, providerMetadata);

        var request = factory.build();
        assertThat(request.getResponseType()).isEqualTo(ResponseType.CODE);
        assertThat(request.getScope()).isEqualTo(providerMetadata.getScopes());
        assertThat(request.getRedirectionURI()).isEqualTo(clientMetadata.getRedirectionURI());
        assertThat(request.getClientID()).isEqualTo(clientInformation.getID());
        assertThat(request.getState()).isInstanceOf(State.class);
        assertThat(request.getNonce()).isInstanceOf(Nonce.class);
        assertThat(request.toURI()).hasHost(serverURI.getHost());
    }
}
