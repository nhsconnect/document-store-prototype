package uk.nhs.digital.docstore.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public interface AuthorizationEnhancer {
    HttpRequest.Builder enhanceWithAuthorization(HttpRequest.Builder original, URI endpoint, String content) throws URISyntaxException;
}
