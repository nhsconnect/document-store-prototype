package uk.nhs.digital.docstore.patientdetails.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignedJwtBuilderTest {

    @Test
    void shouldGenerateASignedJwtWithRequiredClaims() throws MissingEnvironmentVariableException {
        var now = Instant.now();
        var randomUuid = UUID.randomUUID();
        var nhsApiKey = "nhs-api-key";
        var oauthEndpoint = "oauth-endpoint";
        var kid = "some-kid";
        var algorithm = Algorithm.none();
        var patientSearchConfig = mock(PatientSearchConfig.class);

        when(patientSearchConfig.nhsApiKey()).thenReturn(nhsApiKey);
        when(patientSearchConfig.nhsOauthEndpoint()).thenReturn(oauthEndpoint);
        when(patientSearchConfig.kid()).thenReturn(kid);
        when(patientSearchConfig.pdsFhirAuthPrivateTokenSigningAlgorithm()).thenReturn(algorithm);

        var jwtBuilder = new SignedJwtBuilder(patientSearchConfig, now, randomUuid);

        var actualJwt= jwtBuilder.build();

        JWTVerifier verifier = JWT.require(algorithm)
                .withClaim("exp", now.plus(5, ChronoUnit.MINUTES))
                .withJWTId(randomUuid.toString())
                .withIssuer(nhsApiKey)
                .withSubject(nhsApiKey)
                .withAudience(oauthEndpoint)
                .build();

        assertDoesNotThrow(() -> verifier.verify(actualJwt));
    }

}