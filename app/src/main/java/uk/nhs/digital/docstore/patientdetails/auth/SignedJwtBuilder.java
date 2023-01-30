package uk.nhs.digital.docstore.patientdetails.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
import uk.nhs.digital.docstore.utils.CommonUtils;

public class SignedJwtBuilder {
    private final Clock clock;
    private final PatientSearchConfig patientSearchConfig;

    public SignedJwtBuilder(Clock clock, PatientSearchConfig patientSearchConfig) {
        this.clock = clock;
        this.patientSearchConfig = patientSearchConfig;
    }

    public String build() throws MissingEnvironmentVariableException {
        var nhsApiKey = patientSearchConfig.nhsApiKey();
        var nhsOauthEndpoint = patientSearchConfig.nhsOauthEndpoint();

        Algorithm privateAlgorithm = patientSearchConfig.pdsFhirAuthPrivateTokenSigningAlgorithm();

        return JWT.create()
                .withJWTId(CommonUtils.generateRandomUUIDString())
                .withSubject(nhsApiKey)
                .withIssuer(nhsApiKey)
                .withAudience(nhsOauthEndpoint)
                .withExpiresAt(Instant.now(clock).plus(5, ChronoUnit.MINUTES))
                .sign(privateAlgorithm);
    }
}
