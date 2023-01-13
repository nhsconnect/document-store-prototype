package uk.nhs.digital.docstore.patientdetails.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
import uk.nhs.digital.docstore.utils.CommonUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignedJwtBuilderTest {

    @Test
    void shouldGenerateASignedJwtWithRequiredClaims() throws MissingEnvironmentVariableException {
        var now = Instant.now();
        var clock = Clock.fixed(now, ZoneId.systemDefault());
        var randomUuidAsString = UUID.randomUUID().toString();
        var nhsApiKey = "nhs-api-key";
        var oauthEndpoint = "oauth-endpoint";
        var algorithm = Algorithm.none();
        var patientSearchConfig = mock(PatientSearchConfig.class);

        when(patientSearchConfig.nhsApiKey()).thenReturn(nhsApiKey);
        when(patientSearchConfig.nhsOauthEndpoint()).thenReturn(oauthEndpoint);
        when(patientSearchConfig.pdsFhirAuthPrivateTokenSigningAlgorithm()).thenReturn(algorithm);

        var jwtBuilder = new SignedJwtBuilder(clock, patientSearchConfig);

        try (MockedStatic<CommonUtils> utilities = Mockito.mockStatic(CommonUtils.class)) {
            utilities.when(CommonUtils::generateRandomUUIDString).thenReturn(randomUuidAsString);
            var actualJwt= jwtBuilder.build();

            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("exp", now.plus(5, ChronoUnit.MINUTES))
                    .withJWTId(randomUuidAsString)
                    .withIssuer(nhsApiKey)
                    .withSubject(nhsApiKey)
                    .withAudience(oauthEndpoint)
                    .build();

            assertDoesNotThrow(() -> verifier.verify(actualJwt));
        }
    }
}