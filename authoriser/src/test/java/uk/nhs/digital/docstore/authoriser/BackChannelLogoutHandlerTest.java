package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.builders.LogoutTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class BackChannelLogoutHandlerTest {

    private final SignedJWTFactory jwtFactory = new SignedJWTFactory();

    @Test
    public void destroysAllSessionsForASubjectWhenTheLogoutTokenIsValid() throws Exception {
        var jwtClaims = LogoutTokenClaimsSetBuilder.build();
        var validator =
                new LogoutTokenValidator(
                        new Issuer(jwtClaims.getIssuer()),
                        new ClientID(jwtClaims.getAudience().get(0)),
                        JWSAlgorithm.RS256,
                        new JWKSet(jwtFactory.getJWK()));
        var logoutToken = jwtFactory.createJWT(jwtClaims.toJWTClaimsSet());
        var request = new APIGatewayProxyRequestEvent();
        request.setBody("logout_token=" + logoutToken.serialize());

        var sessionOne =
                Session.create(
                        UUID.randomUUID(), 10L, jwtClaims.getSubject(), jwtClaims.getSessionID());
        var sessionTwo =
                Session.create(
                        UUID.randomUUID(), 10L, jwtClaims.getSubject(), jwtClaims.getSessionID());

        var sessionStore = new InMemorySessionStore();
        sessionStore.save(sessionOne);
        sessionStore.save(sessionTwo);

        var handler = new BackChannelLogoutHandler(validator, sessionStore);
        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertThat(response.getStatusCode()).isEqualTo(200);
        Assertions.assertThat(response.getIsBase64Encoded()).isFalse();
        Assertions.assertThat(response.getBody()).isEqualTo("");

        Assertions.assertThat(sessionStore.load(jwtClaims.getSubject(), sessionOne.getId()))
                .isEmpty();
        Assertions.assertThat(sessionStore.load(jwtClaims.getSubject(), sessionTwo.getId()))
                .isEmpty();
    }

    @Test
    public void returnsABadRequestResponseWhenTheLogoutTokenIsInvalid() throws Exception {
        var issuer = new Issuer("test");
        var clientID = new ClientID("test");
        var validator =
                new LogoutTokenValidator(
                        issuer, clientID, JWSAlgorithm.parse("NONE"), new JWKSet());
        var handler = new BackChannelLogoutHandler(validator, new InMemorySessionStore());

        var logoutToken = new PlainJWT(LogoutTokenClaimsSetBuilder.build().toJWTClaimsSet());

        var request = new APIGatewayProxyRequestEvent();
        request.setBody("logout_token=" + logoutToken.serialize());

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertThat(response.getStatusCode()).isEqualTo(400);
    }
}
