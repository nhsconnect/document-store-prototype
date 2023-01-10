package uk.nhs.digital.docstore.patientdetails;

import com.amazonaws.util.Base64;
import com.auth0.jwt.algorithms.Algorithm;
import uk.nhs.digital.docstore.config.Environment;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
        return environment.getEnvVar("NHS_API_KEY", "1CJAXL1fj7RY0uGU62CpGXYE1m1GSA91");
    }

    public String nhsOauthEndpoint() {
        return environment.getEnvVar("NHS_OAUTH_ENDPOINT", "https://int.api.service.nhs.uk/oauth2/token");
    }

    public boolean pdsFhirIsStubbed() {
        return TRUE.equals(environment.getEnvVar("PDS_FHIR_IS_STUBBED", TRUE));
    }

    public String kid() {
        return environment.getEnvVar("KID", "dev-B80F3393-0430-4459-9223-A792AABF813E");
    }

    public Algorithm pdsFhirAuthPrivateTokenSigningAlgorithm() {
        var pemKey = new File("pkcs8.key");
        try {

            var foo = Files.readString(pemKey.toPath());

            String privateKeyAsString = environment.getEnvVar("PDS_FHIR_PRIVATE_KEY", foo)
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

    public Algorithm pdsFhirAuthPublicTokenVerifyAlgorithm() {
        var pemKey = new File("testPem.pem.pub");

        try {
            var foo = Files.readString(pemKey.toPath());

            String publicKeyAsString = environment.getEnvVar("PDS_FHIR_PUBLIC_KEY", foo)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("\\n", "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] keyBytes = Base64.decode(publicKeyAsString.getBytes(StandardCharsets.UTF_8));

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            PublicKey publicKey = factory.generatePublic(keySpec);

            return Algorithm.RSA512((RSAPublicKey) publicKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
