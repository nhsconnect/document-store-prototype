package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import uk.nhs.digital.docstore.authoriser.exceptions.TokenFetchingException;

public class OIDCTokenFetcher {
    public JWT fetchToken(AuthorizationCode authCode) throws TokenFetchingException {
        throw new TokenFetchingException();
    }
}
