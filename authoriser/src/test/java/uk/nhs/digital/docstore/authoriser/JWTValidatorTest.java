package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class JWTValidatorTest {

    @Test
    void shouldReturnDecodedJWTIfValid() throws JsonProcessingException {
        var algorithm = Algorithm.none();
        var token = JWT.create()
                .withSubject("some-principle-id")
                .withClaim("some-claim", "some-value")
                .sign(algorithm);


        var jwtValidator = new JWTValidator(token, algorithm);
        assertDoesNotThrow(() -> jwtValidator.verify());
    }
}
