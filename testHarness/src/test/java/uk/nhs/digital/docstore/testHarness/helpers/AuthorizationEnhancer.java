package uk.nhs.digital.docstore.testHarness.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public interface AuthorizationEnhancer {
    HttpRequest.Builder enhanceWithAuthorization(HttpRequest.Builder original, URI endpoint, String resourcePath, String content) throws URISyntaxException;
}
