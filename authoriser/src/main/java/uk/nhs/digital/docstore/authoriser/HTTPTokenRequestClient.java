package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import java.io.IOException;
import uk.nhs.digital.docstore.authoriser.apiRequestClients.TokenRequestClient;

public class HTTPTokenRequestClient implements TokenRequestClient {
    @Override
    public TokenResponse getResponse(TokenRequest request) {
        try {
            return OIDCTokenResponseParser.parse(request.toHTTPRequest().send());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
