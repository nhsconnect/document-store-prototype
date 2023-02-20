package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import java.net.URI;
import java.net.URISyntaxException;

public class AuthenticationRequestFactory {
    private final AuthenticationRequest.Builder builder;

    public AuthenticationRequestFactory(OIDCClientMetadata metadata) {

        try {
            this.builder =
                    new AuthenticationRequest.Builder(
                            ResponseType.CODE,
                            new Scope("openid"),
                            new ClientID("foo"),
                            new URI("https://our-callback.url"));
            this.builder.endpointURI(metadata.getInitiateLoginURI());
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
