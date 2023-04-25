package uk.nhs.digital.docstore.authoriser.requestEvents;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.id.Subject;
import java.util.*;

public class LogoutRequestEvent extends APIGatewayProxyRequestEvent {
    private Optional<String> getCookie(String cookieName) {

        var headers = getHeaders();

        if (null == headers || headers.isEmpty()) {
            return Optional.empty();
        }

        String cookieData = null;

        if (headers.containsKey("cookie")) {
            cookieData = headers.get("cookie");
        }

        if (headers.containsKey("Cookie")) {
            cookieData = headers.get("Cookie");
        }

        if (cookieData == null || cookieData.isEmpty()) {
            return Optional.empty();
        }

        String[] cookiesArr = cookieData.split(";");

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

    // TODO AKH remove
    public Map<String, String> retrieveHeaders() {
        return getHeaders();
    }
}
