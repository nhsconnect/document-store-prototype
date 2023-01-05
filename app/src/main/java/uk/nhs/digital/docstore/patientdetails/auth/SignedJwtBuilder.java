package uk.nhs.digital.docstore.patientdetails.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class SignedJwtBuilder {
    private final Algorithm algorithm;
    private final Instant now;
    private final UUID uuid;
    private final PatientSearchConfig patientSearchConfig;

    public SignedJwtBuilder(Algorithm algorithm, PatientSearchConfig patientSearchConfig) {
        this(algorithm, Instant.now(), UUID.randomUUID(), patientSearchConfig);
    }

    public SignedJwtBuilder(Algorithm algorithm, Instant now, UUID uuid, PatientSearchConfig patientSearchConfig) {
        this.algorithm = algorithm;
        this.now = now;
        this.uuid = uuid;
        this.patientSearchConfig = patientSearchConfig;
    }

    public String build() {
        var nhsApiKey = patientSearchConfig.nhsApiKey();
        var nhsOauthEndpoint = patientSearchConfig.nhsOauthEndpoint();

        var signedJwt = JWT.create()
                .withJWTId(uuid.toString())
                .withSubject(nhsApiKey)
                .withIssuer(nhsApiKey)
                .withAudience(nhsOauthEndpoint)
                .withExpiresAt(now.plus(5, ChronoUnit.MINUTES));
        return signedJwt.sign(algorithm);
    }
}
