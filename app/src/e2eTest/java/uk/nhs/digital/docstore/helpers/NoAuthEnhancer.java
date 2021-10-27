package uk.nhs.digital.docstore.helpers;

import java.net.URI;
import java.net.http.HttpRequest;

public class NoAuthEnhancer implements AuthorizationEnhancer{

    @Override
    public HttpRequest.Builder enhanceWithAuthorization(HttpRequest.Builder original, URI endpoint, String content) {
        return original;
    }
}
