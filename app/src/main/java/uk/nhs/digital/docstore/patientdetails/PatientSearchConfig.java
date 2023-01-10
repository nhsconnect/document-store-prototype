package uk.nhs.digital.docstore.patientdetails;

import com.auth0.jwt.algorithms.Algorithm;
import uk.nhs.digital.docstore.config.Environment;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
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

    public String pdsFhirRootUri() {
        return environment.getEnvVar("PDS_FHIR_ENDPOINT", "https://sandbox.api.service.nhs.uk/personal-demographics/FHIR/R4/");
    }

    public String nhsApiKey() throws MissingEnvironmentVariableException {
        return environment.getEnvVar("NHS_API_KEY");
    }

    public String nhsOauthEndpoint() throws MissingEnvironmentVariableException {
        return environment.getEnvVar("NHS_OAUTH_ENDPOINT");
    }

    public boolean pdsFhirIsStubbed() {
        return TRUE.equals(environment.getEnvVar("PDS_FHIR_IS_STUBBED", TRUE));
    }

    public String kid() throws MissingEnvironmentVariableException {
        return environment.getEnvVar("KID");
    }

    public Algorithm pdsFhirAuthPrivateTokenSigningAlgorithm() {
        return Algorithm.RSA512(new SigningKeyProvider(environment));
    }
}
