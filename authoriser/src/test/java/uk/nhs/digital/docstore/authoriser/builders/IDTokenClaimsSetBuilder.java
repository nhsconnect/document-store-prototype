package uk.nhs.digital.docstore.authoriser.builders;

import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.id.ClientID;
import java.time.Instant;
import java.util.UUID;

public class IDTokenClaimsSetBuilder {
    public IDTokenClaimsSetBuilder() {}

    public static JWTClaimsSet buildClaimsSet() {
        var issuer = "http://issuer.url";
        var clientID = new ClientID("test");
        var issuedAt = Instant.now().getEpochSecond();
        var expirationTime = Instant.now().plusSeconds(500).getEpochSecond();
        var sub = UUID.randomUUID();
        var claimsSetBuilder = new JWTClaimsSet.Builder();
        return claimsSetBuilder
                .claim(JWTClaimNames.EXPIRATION_TIME, expirationTime)
                .claim(JWTClaimNames.ISSUER, issuer)
                .claim(JWTClaimNames.SUBJECT, sub.toString())
                .claim(JWTClaimNames.AUDIENCE, clientID.toString())
                .claim(JWTClaimNames.ISSUED_AT, issuedAt)
                .build();
    }
}
