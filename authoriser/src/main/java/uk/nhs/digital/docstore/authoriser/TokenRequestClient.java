package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;

public interface TokenRequestClient {
    TokenResponse getResponse(TokenRequest request);
}
