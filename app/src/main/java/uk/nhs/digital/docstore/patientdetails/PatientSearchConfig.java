package uk.nhs.digital.docstore.patientdetails;

import com.amazonaws.util.Base64;
import com.auth0.jwt.algorithms.Algorithm;
import uk.nhs.digital.docstore.config.Environment;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

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

    public String nhsApiKey() {
        return environment.getEnvVar("NHS_API_KEY", "");
    }

    public String nhsOauthEndpoint() {
        return environment.getEnvVar("NHS_OAUTH_ENDPOINT", "");
    }

    public boolean pdsFhirIsStubbed() {
        return TRUE.equals(environment.getEnvVar("PDS_FHIR_IS_STUBBED", TRUE));
    }

    public Algorithm pdsFhirAuthTokenSigningAlgorithm() {
        try {
            String privateKeyAsString = environment.getEnvVar("PDS_FHIR_PRIVATE_KEY")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("\\n", "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] keyBytes = Base64.decode(privateKeyAsString.getBytes(StandardCharsets.UTF_8));

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = factory.generatePrivate(keySpec);
            return Algorithm.RSA512((RSAPrivateKey) privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
