package uk.nhs.digital.docstore.patientdetails;

import static uk.nhs.digital.docstore.utils.KmsKeyDecrypt.decryptCiphertextWithKey;

import com.auth0.jwt.algorithms.Algorithm;
import uk.nhs.digital.docstore.config.Environment;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.auth.SigningKeyProvider;

public class PatientSearchConfig {

    public static final String TRUE = "true";
    private final Environment environment;

    public PatientSearchConfig() {
        this(new Environment());
    }

    public PatientSearchConfig(Environment environment) {
        this.environment = environment;
    }

    public String pdsFhirRootUri() throws MissingEnvironmentVariableException {
        return environment.getEnvVar("PDS_FHIR_ENDPOINT");
    }

    public String nhsApiKey() throws MissingEnvironmentVariableException {
        return decryptCiphertextWithKey(environment.getEnvVar("NHS_API_KEY"));
        //        return environment.getEnvVar("NHS_API_KEY");
    }

    public String nhsOauthEndpoint() throws MissingEnvironmentVariableException {
        return environment.getEnvVar("NHS_OAUTH_ENDPOINT");
    }

    public boolean pdsFhirIsStubbed() {
        return TRUE.equals(environment.getEnvVar("PDS_FHIR_IS_STUBBED", TRUE));
    }

    public String pdsFhirTokenName() throws MissingEnvironmentVariableException {
        return environment.getEnvVar("PDS_FHIR_TOKEN_NAME");
    }

    public Algorithm pdsFhirAuthPrivateTokenSigningAlgorithm() {
        return Algorithm.RSA512(new SigningKeyProvider(environment));
    }
}
