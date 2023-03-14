package uk.nhs.digital.docstore.authoriser.requests;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.State;
import java.util.*;

public class TokenRequestEvent extends APIGatewayProxyRequestEvent {

    public Optional<State> getCookieState() {
        var headers = getHeaders();

        if (headers == null || headers.get("cookie") == null) {
            return Optional.empty();
        }

        var cookiesString = headers.get("cookie");
        var cookies = new HashMap<String, String>();

        Arrays.stream(cookiesString.split(";"))
                .forEach(
                        cookieString -> {
                            var keyValueTuple = cookieString.split("=");
                            cookies.put(keyValueTuple[0].trim(), keyValueTuple[1].trim());
                        });

        return Optional.ofNullable(cookies.get("State")).map(State::new);
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
