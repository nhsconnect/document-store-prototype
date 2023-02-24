package uk.nhs.digital.docstore.authoriser.requests;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import java.net.HttpCookie;
import java.util.Optional;
import java.util.UUID;

public class LogoutRequestEvent extends APIGatewayProxyRequestEvent {
    public Optional<UUID> getSessionId() {
        var headers = getHeaders();
        if (headers == null) {
            return Optional.empty();
        }
        var cookies = HttpCookie.parse(headers.get("Cookie"));
        var sessionIdCookie =
                cookies.stream()
                        .filter(httpCookie -> httpCookie.getName().equals("SessionId"))
                        .findFirst();
        return sessionIdCookie.map(HttpCookie::getValue).map(UUID::fromString);
    }

    public Optional<String> getRedirectUri() {
        return Optional.ofNullable(getQueryStringParameters())
                .map(parameters -> parameters.get("redirect_uri"));
    }
}
