package uk.nhs.digital.docstore.authoriser.requestEvents;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.id.Subject;
import java.util.*;

public class LogoutRequestEvent extends APIGatewayProxyRequestEvent {
    private Optional<String> getCookie(String cookieName) {
        var headers = getHeaders();

        if (headers == null || headers.get("cookie") == null || headers.get("cookie").isEmpty()) {
            return Optional.empty();
        }

        String[] cookiesArr = headers.get("cookie").split(";");

        Map<String, String> cookiesMap = new HashMap<>();
        String[] cookieSplits;

        for (String cookie : cookiesArr) {
            cookieSplits = cookie.trim().split("=");
            cookiesMap.put(cookieSplits[0], cookieSplits[1]);
        }

        return Optional.ofNullable(cookiesMap.get(cookieName));
    }

    public Optional<UUID> getSessionId() {
        return getCookie("SessionId").map(UUID::fromString);
    }

    public Optional<Subject> getSubject() {
        return getCookie("SubjectClaim").map(Subject::new);
    }

    public Optional<String> getRedirectUri() {
        return Optional.ofNullable(getQueryStringParameters())
                .map(parameters -> parameters.get("redirect_uri"));
    }

    // TODO AKH delete
    public Map<String, String> dumpCookie() {
        var headers = getHeaders();

        if (headers == null || headers.get("cookie") == null || headers.get("cookie").isEmpty()) {
            return null;
        }

        String[] cookiesArr = headers.get("cookie").split(";");

        Map<String, String> cookiesMap = new HashMap<>();
        String[] cookieSplits;

        for (String cookie : cookiesArr) {
            cookieSplits = cookie.trim().split("=");
            cookiesMap.put(cookieSplits[0], cookieSplits[1]);
        }

        return cookiesMap;
    }
}
