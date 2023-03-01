package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;

public class AuthenticationRequestFactory {
    private final AuthenticationRequest.Builder builder;

    public AuthenticationRequestFactory(
            OIDCClientInformation clientInformation, OIDCProviderMetadata providerMetadata) {
        this.builder =
                new AuthenticationRequest.Builder(
                        ResponseType.CODE,
                        providerMetadata.getScopes(),
                        clientInformation.getID(),
                        clientInformation.getOIDCMetadata().getRedirectionURI());
        this.builder.endpointURI(providerMetadata.getAuthorizationEndpointURI());
    }

    public AuthenticationRequest build() {
        builder.state(new State());
        builder.nonce(new Nonce());
        return builder.build();
    }
}
