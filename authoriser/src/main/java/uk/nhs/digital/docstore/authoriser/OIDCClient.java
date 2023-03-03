package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.models.Session;

public interface OIDCClient {

    Session authoriseSession(AuthorizationCode authCode) throws AuthorisationException;
}
