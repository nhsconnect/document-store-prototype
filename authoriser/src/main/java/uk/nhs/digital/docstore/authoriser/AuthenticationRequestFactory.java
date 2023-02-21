package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import java.net.URI;
import java.net.URISyntaxException;

public class AuthenticationRequestFactory {
    private final AuthenticationRequest.Builder builder;

    public AuthenticationRequestFactory(OIDCClientConfig oidcClientConfig) {

        try {
            this.builder =
                    new AuthenticationRequest.Builder(
                            ResponseType.CODE,
                            new Scope("openid"),
                            new ClientID(oidcClientConfig.getClientID()),
                            new URI(oidcClientConfig.getCallbackURL()));
            this.builder.endpointURI(new URI(oidcClientConfig.getAuthorizeURL()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticationRequest build() {
        builder.state(new State());
        builder.nonce(new Nonce());
        return builder.build();
    }
}
