package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.util.Optional;
import uk.nhs.digital.docstore.authoriser.models.Session;

public interface CIS2Client {

    Optional<Session> authoriseSession(AuthorizationCode authCode);
}
