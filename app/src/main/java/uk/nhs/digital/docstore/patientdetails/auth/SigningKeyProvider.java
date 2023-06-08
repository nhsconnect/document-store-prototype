package uk.nhs.digital.docstore.patientdetails.auth;

import com.amazonaws.util.Base64;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import uk.nhs.digital.docstore.config.Environment;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.utils.SSMService;

public class SigningKeyProvider implements RSAKeyProvider {
    private final Environment environment;
    private final SSMService ssmService;

    public SigningKeyProvider(Environment environment, SSMService ssmService) {
        this.environment = environment;
        this.ssmService = ssmService;
    }

    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
        return null;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        try {
            String privateKeySSMValue =
                    ssmService.retrieveParameterStoreValue(
                            environment.getEnvVar("PDS_FHIR_PRIVATE_KEY"));

            String privateKeyAsString =
                    privateKeySSMValue
                            .replace("-----BEGIN PRIVATE KEY-----", "")
                            .replaceAll("\\n", "")
                            .replace("-----END PRIVATE KEY-----", "");

            byte[] keyBytes = Base64.decode(privateKeyAsString.getBytes(StandardCharsets.UTF_8));

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            PrivateKey privateKey = factory.generatePrivate(keySpec);

            return (RSAPrivateKey) privateKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPrivateKeyId() {
        try {
            return environment.getEnvVar("PDS_FHIR_KID");
        } catch (MissingEnvironmentVariableException e) {
            throw new RuntimeException(e);
        }
    }
}
