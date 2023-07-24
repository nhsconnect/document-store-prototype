package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.models.Session;

public interface OIDCClient {

    Session authoriseSession(AuthorizationCode authCode) throws LoginException;

    UserInfo fetchUserInfo(String sessionID, String subClaim) throws LoginException;
}
