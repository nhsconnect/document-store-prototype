package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTValidator {
    private final String jwt;

    private final Algorithm algorithm;

    public JWTValidator(String jwt, Algorithm algorithm) {
        this.jwt = jwt;
        this.algorithm = algorithm;
    }

    public DecodedJWT verify() {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(jwt);
    }
}
