package uk.nhs.digital.docstore.patientdetails.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SignedJwtBuilder {
    private final Instant now;
    private final UUID uuid;
    private final PatientSearchConfig patientSearchConfig;

    public SignedJwtBuilder(PatientSearchConfig patientSearchConfig) {
        this(patientSearchConfig, Instant.now(), UUID.randomUUID());
    }

    public SignedJwtBuilder(PatientSearchConfig patientSearchConfig, Instant now, UUID uuid) {
        this.now = now;
        this.uuid = uuid;
        this.patientSearchConfig = patientSearchConfig;
    }

    public String build() throws MissingEnvironmentVariableException {
        var nhsApiKey = patientSearchConfig.nhsApiKey();
        var nhsOauthEndpoint = patientSearchConfig.nhsOauthEndpoint();

        Algorithm privateAlgorithm = patientSearchConfig.pdsFhirAuthPrivateTokenSigningAlgorithm();

        return JWT.create()
                .withJWTId(uuid.toString())
                .withSubject(nhsApiKey)
                .withIssuer(nhsApiKey)
                .withAudience(nhsOauthEndpoint)
                .withExpiresAt(now.plus(5, ChronoUnit.MINUTES))
                .sign(privateAlgorithm);
    }
}
