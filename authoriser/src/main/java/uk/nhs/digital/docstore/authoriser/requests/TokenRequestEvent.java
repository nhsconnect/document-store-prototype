package uk.nhs.digital.docstore.authoriser.requests;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.net.HttpCookie;
import java.util.Optional;
import java.util.UUID;

public class TokenRequestEvent extends APIGatewayProxyRequestEvent {
    public Optional<UUID> getSessionId() {
        var headers = getHeaders();
        if (headers == null || headers.get("Cookie") == null) {
            return Optional.empty();
        }
        var cookies = HttpCookie.parse(headers.get("Cookie"));
        var sessionIdCookie =
                cookies.stream()
                        .filter(httpCookie -> httpCookie.getName().equals("SessionId"))
                        .findFirst();
        return sessionIdCookie.map(HttpCookie::getValue).map(UUID::fromString);
    }

    public Optional<AuthorizationCode> getAuthCode() {
        return Optional.ofNullable(getQueryStringParameters())
                .map(parameters -> parameters.get("code"))
                .map(AuthorizationCode::new);
    }

    public Optional<String> getRedirectUri() {
        return Optional.ofNullable(getQueryStringParameters())
                .map(parameters -> parameters.get("redirect_uri"));
    }
}
