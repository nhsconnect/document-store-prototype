package uk.nhs.digital.docstore.patientdetails.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
import uk.nhs.digital.docstore.utils.CommonUtils;

public class SignedJwtBuilder {
    private final PatientSearchConfig patientSearchConfig;

    public SignedJwtBuilder(PatientSearchConfig patientSearchConfig) {
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
                .withExpiresAt(CommonUtils.generateExpiryDate())
                .sign(privateAlgorithm);
    }
}
