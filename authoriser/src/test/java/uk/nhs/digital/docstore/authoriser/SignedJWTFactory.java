package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.*;

public class SignedJWTFactory {
    private final RSAKey signingKey;

    public SignedJWTFactory() {
        try {
            signingKey =
                    new RSAKeyGenerator(2048)
                            .keyUse(KeyUse.SIGNATURE)
                            .keyIDFromThumbprint(true)
                            .generate();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public SignedJWT createJWT(JWTClaimsSet claimsSet) {
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
        try {
            signedJWT.sign(new RSASSASigner(signingKey));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        return signedJWT;
    }

    public JWK getJWK() {
        return signingKey.toPublicJWK();
    }
}
