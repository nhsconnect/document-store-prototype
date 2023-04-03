package uk.nhs.digital.docstore.authoriser.requestEvents;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.id.Subject;
import java.util.*;

public class AuthoriserRequestEvent extends APIGatewayProxyRequestEvent {
    public Optional<Subject> getSubject() {
        return getCookie("SubjectClaim").map(Subject::new);
    }

    public Optional<UUID> getSessionId() {
        return getCookie("SessionId").map(UUID::fromString);
    }

    private Optional<String> getCookie(String cookieName) {
        var headers = getHeaders();

        var hasUpper = headers != null && headers.containsKey("Cookie");

        var cookieKey = hasUpper ? "Cookie" : "cookie";

        var hasNoCookie =
                headers == null
                        || headers.get(cookieKey) == null
                        || headers.get(cookieKey).isEmpty();

        if (hasNoCookie) {
            return Optional.empty();
        }

        var cookieHeader = headers.get(cookieKey);

        var cookies = new HashMap<String, String>();

        Arrays.stream(cookieHeader.split(";"))
                .forEach(
                        cookieString -> {
                            var keyValueTuple = cookieString.split("=");
                            cookies.put(keyValueTuple[0].trim(), keyValueTuple[1].trim());
                        });

        return Optional.ofNullable(cookies.get(cookieName));
    }
}
