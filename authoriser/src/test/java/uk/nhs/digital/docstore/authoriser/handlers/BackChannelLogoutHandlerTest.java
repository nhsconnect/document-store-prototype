package uk.nhs.digital.docstore.authoriser.handlers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import com.nimbusds.openid.connect.sdk.validators.LogoutTokenValidator;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.SignedJWTFactory;
import uk.nhs.digital.docstore.authoriser.builders.LogoutTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class BackChannelLogoutHandlerTest {
    private final SignedJWTFactory jwtFactory = new SignedJWTFactory();

    @Test
    public void destroysCurrentSessionWhenTheLogoutTokenIsValid() throws Exception {
        var jwtClaims = LogoutTokenClaimsSetBuilder.build();
        var tokenValidator =
                new LogoutTokenValidator(
                        new Issuer(jwtClaims.getIssuer()),
                        new ClientID(jwtClaims.getAudience().get(0)),
                        JWSAlgorithm.RS256,
                        new JWKSet(jwtFactory.getJWK()));
        var logoutToken = jwtFactory.createJWT(jwtClaims.toJWTClaimsSet());
        var timeToExist = Instant.ofEpochSecond(10L);
        var sessionOne =
                Session.create(
                        UUID.randomUUID(),
                        timeToExist,
                        jwtClaims.getSubject(),
                        jwtClaims.getSessionID());
        var sessionTwo =
                Session.create(
                        UUID.randomUUID(),
                        timeToExist,
                        jwtClaims.getSubject(),
                        new SessionID("ses2"));
        var sessionStore = new InMemorySessionStore();
        var request =
                new APIGatewayProxyRequestEvent()
                        .withBody("logout_token=" + logoutToken.serialize());
        var handler = new BackChannelLogoutHandler(tokenValidator, sessionStore);

        sessionStore.save(sessionOne);
        sessionStore.save(sessionTwo);
        var response = handler.handleRequest(request, mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getBody()).isEqualTo("");
        assertThat(sessionStore.load(jwtClaims.getSubject(), sessionOne.getId())).isEmpty();
        assertThat(sessionStore.load(jwtClaims.getSubject(), sessionTwo.getId())).isNotEmpty();
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
        var request =
                new APIGatewayProxyRequestEvent()
                        .withBody("logout_token=" + logoutToken.serialize());

        var response = handler.handleRequest(request, mock(Context.class));

        assertThat(response.getStatusCode()).isEqualTo(400);
    }
}
