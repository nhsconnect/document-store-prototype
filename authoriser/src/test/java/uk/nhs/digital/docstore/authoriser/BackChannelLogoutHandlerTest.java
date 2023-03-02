package uk.nhs.digital.docstore.authoriser;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BackChannelLogoutHandlerTest {

    private final SignedJWTFactory jwtFactory = new SignedJWTFactory();

    @Test
    public void destroysTheSessionWhenTheSessionIdIsValid() throws Exception {
        var jwtClaims = LogoutTokenClaimsSetBuilder.build().toJWTClaimsSet();
        var validator =
                new LogoutTokenValidator(
                        new Issuer(jwtClaims.getIssuer()),
                        new ClientID(jwtClaims.getAudience().get(0)),
                        JWSAlgorithm.RS256,
                        new JWKSet(jwtFactory.getJWK()));
        var logoutToken = jwtFactory.createJWT(jwtClaims);
        var request = new APIGatewayProxyRequestEvent();
        request.setBody("logout_token=" + logoutToken.serialize());

        var handler = new BackChannelLogoutHandler(validator);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(response.getIsBase64Encoded()).isFalse();
        Assertions.assertThat(response.getBody()).isEqualTo("");
    }

    @Test
    public void returnsABadRequestResponseWhenTheLogoutTokenIsInvalid() throws Exception {
        var issuer = new Issuer("test");
        var clientID = new ClientID("test");
        var validator =
                new LogoutTokenValidator(
                        issuer, clientID, JWSAlgorithm.parse("NONE"), new JWKSet());
        var handler = new BackChannelLogoutHandler(validator);

        var logoutToken = new PlainJWT(LogoutTokenClaimsSetBuilder.build().toJWTClaimsSet());

        var request = new APIGatewayProxyRequestEvent();
        request.setBody("logout_token=" + logoutToken.serialize());

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertThat(response.getStatusCode()).isEqualTo(400);
    }
}
