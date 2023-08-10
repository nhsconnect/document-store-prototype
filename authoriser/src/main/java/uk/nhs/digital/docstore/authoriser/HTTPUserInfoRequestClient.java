package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import java.io.IOException;
import uk.nhs.digital.docstore.authoriser.apiRequestClients.UserInfoRequestClient;

public class HTTPUserInfoRequestClient implements UserInfoRequestClient {
    @Override
    public UserInfoResponse getResponse(UserInfoRequest request) {
        try {
            var httpRequest = request.toHTTPRequest();
            return UserInfoResponse.parse(httpRequest.send());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
