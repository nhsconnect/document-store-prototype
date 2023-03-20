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

        if (headers == null || headers.get("cookie") == null || headers.get("cookie").isEmpty()) {
            return Optional.empty();
        }

        var cookieHeader = headers.get("cookie");
        var cookies = new HashMap<String, String>();

        Arrays.stream(cookieHeader.split(";"))
                .forEach(
                        cookie -> {
                            var keyValue = cookie.split("=");
                            cookies.put(keyValue[0].trim(), keyValue[1].trim());
                        });

        return Optional.ofNullable(cookies.get(cookieName));
    }
}
