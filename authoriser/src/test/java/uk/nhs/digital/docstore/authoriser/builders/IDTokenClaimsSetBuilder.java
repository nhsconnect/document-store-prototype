package uk.nhs.digital.docstore.authoriser.builders;

import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import java.time.Instant;
import java.util.UUID;

public class IDTokenClaimsSetBuilder {
    public IDTokenClaimsSetBuilder() {}

    public static IDTokenClaimsSet buildClaimsSet() {
        var issuer = "http://issuer.url";
        var clientID = new ClientID("test");
        var issuedAt = Instant.now().getEpochSecond();
        var expirationTime = Instant.now().plusSeconds(500).getEpochSecond();
        var sub = UUID.randomUUID();
        var sid = UUID.randomUUID();
        var claimsSetBuilder = new JWTClaimsSet.Builder();

        IDTokenClaimsSet claimsSet;
        try {
            claimsSet =
                    new IDTokenClaimsSet(
                            claimsSetBuilder
                                    .claim(JWTClaimNames.EXPIRATION_TIME, expirationTime)
                                    .claim(JWTClaimNames.ISSUER, issuer)
                                    .claim(JWTClaimNames.SUBJECT, sub.toString())
                                    .claim(JWTClaimNames.AUDIENCE, clientID.toString())
                                    .claim(JWTClaimNames.ISSUED_AT, issuedAt)
                                    .claim(IDTokenClaimsSet.SID_CLAIM_NAME, sid)
                                    .build());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return claimsSet;
    }
}
