package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class JWTValidatorTest {

    @Test
    void shouldReturnDecodedJWTIfValid() {
        try (MockedStatic<Environment> env = Mockito.mockStatic(Environment.class)) {
            String issuer = "some-issuer";
            env.when(() -> Environment.get("COGNITO_PUBLIC_KEY_URL")).thenReturn(issuer);

            var algorithm = Algorithm.none();
            var token = JWT.create()
                    .withSubject("some-principle-id")
                    .withClaim("some-claim", "some-value")
                    .withIssuer(issuer)
                    .sign(algorithm);

            var jwtValidator = new JWTValidator(token, algorithm);
            assertDoesNotThrow(() -> jwtValidator.verify());
        }
    }

    @Test
    void shouldThrowInvalidJWTExceptionIfInvalidJWT() {
        var algorithm = Algorithm.none();
        var token = "invalid-token";

        var jwtValidator = new JWTValidator(token, algorithm);

        assertThrows(InvalidJWTException.class, jwtValidator::verify);
    }
}
