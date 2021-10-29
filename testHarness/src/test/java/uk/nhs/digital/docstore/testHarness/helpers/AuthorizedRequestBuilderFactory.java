package uk.nhs.digital.docstore.testHarness.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class AuthorizedRequestBuilderFactory {
        public static HttpRequest.Builder newBuilder(URI endpoint, String path, String content) throws URISyntaxException {
            HttpRequest.Builder original = HttpRequest.newBuilder(endpoint.resolve(path));

            boolean isLocalStack = System.getenv("API_AUTH") == null;
            if (isLocalStack) {
               return original;
            }

            return new AwsIamEnhancer().enhanceWithAuthorization(original, endpoint, path, content);
        }
}
