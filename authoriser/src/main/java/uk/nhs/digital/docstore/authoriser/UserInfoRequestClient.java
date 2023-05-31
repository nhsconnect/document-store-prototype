package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;

public interface UserInfoRequestClient {
    UserInfoResponse getResponse(UserInfoRequest request);
}
