package uk.nhs.digital.docstore.testHarness.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class AuthorizedRequestBuilderFactory {
    public static HttpRequest.Builder newPostBuilder(String endpoint, String path, String content) throws URISyntaxException {
        HttpRequest.Builder original = HttpRequest.newBuilder(new URI(endpoint + "/" + path));
        original.POST(HttpRequest.BodyPublishers.ofString(content));
        return addAuth(original, endpoint, path, content);
    }

    public static HttpRequest.Builder newGetBuilder(String endpoint, String path) throws URISyntaxException {
        HttpRequest.Builder original = HttpRequest.newBuilder(new URI(endpoint + "/" + path));
        original.GET();
        return addAuth(original, endpoint, path, null);
    }

    public static HttpRequest.Builder addAuth(HttpRequest.Builder original, String endpoint, String path, String content) throws URISyntaxException {

        boolean isLocalStack = System.getenv("API_AUTH") == null;
        if (isLocalStack) {
            return original;
        }

        return new AwsIamEnhancer().enhanceWithAuthorization(original, new URI(endpoint), path, content);
    }
}
