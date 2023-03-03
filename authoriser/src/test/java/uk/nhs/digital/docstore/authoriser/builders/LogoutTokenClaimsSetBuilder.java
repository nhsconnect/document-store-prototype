package uk.nhs.digital.docstore.authoriser.builders;

import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.id.JWTID;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.LogoutTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class LogoutTokenClaimsSetBuilder {
    public static LogoutTokenClaimsSet build() {
        return new LogoutTokenClaimsSet(
                new Issuer("foo"),
                new Subject("bar"),
                List.of(new Audience("baz")),
                Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)),
                new JWTID("qux"),
                new SessionID("ses"));
    }
}
