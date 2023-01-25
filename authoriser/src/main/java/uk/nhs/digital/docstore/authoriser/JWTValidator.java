package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWTValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTValidator.class);

    private final String jwt;
    private final Algorithm algorithm;

    public JWTValidator(String jwt, Algorithm algorithm) {
        this.jwt = jwt;
        this.algorithm = algorithm;
    }

    public DecodedJWT verify() throws InvalidJWTException {
        try {
            LOGGER.debug("Verify JWT " + jwt);
            LOGGER.debug("Verify JWT Algorithm" + algorithm.getName() + " " + algorithm.getSigningKeyId());

            var publicKey = Environment.get("COGNITO_PUBLIC_KEY_URL");
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(publicKey).build();

            LOGGER.debug("Verify JWT Algorithm verify" + verifier);
            return verifier.verify(jwt);
        } catch (Exception e) {
            LOGGER.debug("Exception Message" + e);
            throw new InvalidJWTException();
        }
    }
}
