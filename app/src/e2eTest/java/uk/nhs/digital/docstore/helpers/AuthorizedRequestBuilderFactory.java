package uk.nhs.digital.docstore.helpers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class AuthorizedRequestBuilderFactory {
        public static HttpRequest.Builder newBuilder(URI endpoint, String path, String content) throws URISyntaxException {
            HttpRequest.Builder original = HttpRequest.newBuilder(endpoint.resolve(path));

            boolean isLocalStack = System.getenv("DOCUMENT_STORE_BASE_URI") == null;
            if (isLocalStack) {
               return original;
            }

            return new AwsIamEnhancer().enhanceWithAuthorization(original, endpoint, content);
        }
}
