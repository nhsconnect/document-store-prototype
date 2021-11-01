package uk.nhs.digital.docstore.testHarness.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class AuthorizedRequestBuilderFactory {
        public static HttpRequest.Builder newBuilder(String endpoint, String path, String method, String content) throws URISyntaxException {
            HttpRequest.Builder original = HttpRequest.newBuilder(new URI(endpoint+"/"+path));
            if (method.equals("GET")) {
                original.GET();
            }

            if (method.equals("POST")) {
                original.POST(HttpRequest.BodyPublishers.ofString(content));
            }

            boolean isLocalStack = System.getenv("API_AUTH") == null;
            if (isLocalStack) {
               return original;
            }

            return new AwsIamEnhancer().enhanceWithAuthorization(original, new URI(endpoint), path, content);
        }
}
