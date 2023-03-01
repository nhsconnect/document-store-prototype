package uk.nhs.digital.docstore.authoriser;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbusds.jwt.*;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.time.Instant;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class CIS2HttpClientTest {

    @Test
    void createsAUserSessionWhenTheAuthCodeCanBeExchangedForAValidIdToken()
            throws TokenFetchingException {
        var authCode = new AuthorizationCode();
        var sessionStore = new InMemorySessionStore();
        var tokenFetcher = Mockito.mock(OIDCTokenFetcher.class);

        var expirationTime = Instant.now().getEpochSecond();
        var claimsSetBuilder = new JWTClaimsSet.Builder();
        var claimsSet =
                claimsSetBuilder.claim(JWTClaimNames.EXPIRATION_TIME, expirationTime).build();
        var idToken = new PlainJWT(claimsSet);
        Mockito.when(tokenFetcher.fetchToken(authCode)).thenReturn(idToken);

        var client = new CIS2HttpClient(sessionStore, tokenFetcher);

        var optionalResult = client.authoriseSession(authCode);
        Assertions.assertThat(optionalResult).isPresent();
        var result = optionalResult.get();

        var optionalSession = sessionStore.load(result.getId());
        Assertions.assertThat(optionalSession).isPresent();
        var session = optionalSession.get();

        Assertions.assertThat(session.getTimeToExist()).isEqualTo(expirationTime);
    }
}
