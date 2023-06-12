package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;

public interface OIDCClient {

    Session authoriseSession(AuthorizationCode authCode) throws AuthorisationException;

    UserInfo fetchUserInfo(String sessionID, String subClaim) throws UserInfoFetchingException;
}
