package uk.nhs.digital.docstore.authoriser.requests;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.State;
import java.net.HttpCookie;
import java.util.Optional;

public class TokenRequestEvent extends APIGatewayProxyRequestEvent {
    public Optional<State> getCookieState() {
        var headers = getHeaders();
        if (headers == null || headers.get("Cookie") == null) {
            return Optional.empty();
        }
        var cookies = HttpCookie.parse(headers.get("Cookie"));
        var stateCookie =
                cookies.stream()
                        .filter(httpCookie -> httpCookie.getName().equals("State"))
                        .findFirst();
        return stateCookie.map(HttpCookie::getValue).map(State::new);
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

    public Optional<State> getQueryParameterState() {
        return Optional.ofNullable(getQueryStringParameters())
                .map(parameters -> parameters.get("state"))
                .map(State::new);
    }

    public boolean hasMatchingStateValues() {
        var queryParameterState = getQueryParameterState();
        var cookieState = getCookieState();
        return queryParameterState.isPresent()
                && cookieState.isPresent()
                && queryParameterState.get().equals(cookieState.get());
    }
}
