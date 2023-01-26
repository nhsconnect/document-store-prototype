package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JWTValidatorTest {

    @Test
    void shouldReturnDecodedJWTIfValid() {
        var algorithm = Algorithm.none();
        var token = JWT.create()
                .withSubject("some-principle-id")
                .withClaim("some-claim", "some-value")
                .sign(algorithm);


        var jwtValidator = new JWTValidator(token, algorithm);
        assertDoesNotThrow(() -> jwtValidator.verify());
    }

    @Test
    void shouldThrowInvalidJWTExceptionIfInvalidJWT() {
        var algorithm = Algorithm.none();
        var token = "invalid-token";

        var jwtValidator = new JWTValidator(token, algorithm);

        assertThrows(InvalidJWTException.class, jwtValidator::verify);
    }
}
