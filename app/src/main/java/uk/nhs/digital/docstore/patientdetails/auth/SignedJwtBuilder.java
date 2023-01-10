package uk.nhs.digital.docstore.patientdetails.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
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

    public String build() {
        var nhsApiKey = patientSearchConfig.nhsApiKey();
        var nhsOauthEndpoint = patientSearchConfig.nhsOauthEndpoint();

        Algorithm privateAlgorithm = patientSearchConfig.pdsFhirAuthPrivateTokenSigningAlgorithm();

        var signedJwt = JWT.create()
                .withHeader(Map.of("alg", "RS512", "typ", "JWT", "kid", patientSearchConfig.kid()))
                .withJWTId(uuid.toString())
                .withSubject(nhsApiKey)
                .withIssuer(nhsApiKey)
                .withAudience(nhsOauthEndpoint)
                .withClaim("exp", now.plus(5, ChronoUnit.MINUTES).getEpochSecond()).sign(privateAlgorithm);

        Algorithm publicAlgorithm = patientSearchConfig.pdsFhirAuthPublicTokenVerifyAlgorithm();

        JWTVerifier verifier = JWT.require(publicAlgorithm)
                .withJWTId(uuid.toString())
                .withSubject(nhsApiKey)
                .withIssuer(nhsApiKey)
                .withAudience(nhsOauthEndpoint)
                .withClaim("exp", now.plus(5, ChronoUnit.MINUTES).getEpochSecond()).build();

        verifier.verify(signedJwt);

        return signedJwt;
    }
}
