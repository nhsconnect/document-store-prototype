package uk.nhs.digital.docstore.testHarness.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class AuthorizedRequestBuilderFactory {
    private static final String endpoint = System.getenv("DOCUMENT_STORE_BASE_URI");

    public static HttpRequest.Builder newPostBuilder(String path, String content) throws URISyntaxException {
        System.out.println("Base URI: " + endpoint);
        HttpRequest.Builder original = HttpRequest.newBuilder(new URI(endpoint + "/" + path));
        original.POST(HttpRequest.BodyPublishers.ofString(content));
        return addAuth(original, path, content);
    }

    public static HttpRequest.Builder newGetBuilder(String path) throws URISyntaxException {
        HttpRequest.Builder original = HttpRequest.newBuilder(new URI(endpoint + "/" + path));
        original.GET();
        return addAuth(original, path, null);
    }

    public static HttpRequest.Builder addAuth(HttpRequest.Builder original, String path, String content) throws URISyntaxException {

        boolean isLocalStack = System.getenv("API_AUTH") == null;
        if (isLocalStack) {
            return original;
        }

        return new AwsIamEnhancer().enhanceWithAuthorization(original, new URI(endpoint), path, content);
    }
}
