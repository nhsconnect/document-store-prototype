package uk.nhs.digital.docstore.authoriser.requestEvents;

import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenRequestEventTest {
    @Test
    void getCookieStateParsesSingleValueCookieHeaderWithState() {
        var stateValue = "value";
        var headers = Map.of("Cookie", "State=" + stateValue);
        var tokenRequestEvent = new TokenRequestEvent();
        tokenRequestEvent.setHeaders(headers);

        var cookieState = tokenRequestEvent.getCookieState();

        Assertions.assertThat(cookieState).isPresent();
        Assertions.assertThat(cookieState.get().getValue()).isEqualTo(stateValue);
    }

    @Test
    void getCookieStateParsesLowercaseCookieHeader() {
        var headers = Map.of("cookie", "State=some-state");
        var tokenRequestEvent = new TokenRequestEvent();
        tokenRequestEvent.setHeaders(headers);

        var cookieState = tokenRequestEvent.getCookieState();

        Assertions.assertThat(cookieState).isPresent();
    }

    @Test
    void getCookieStateParsesMultiValueCookieHeaderWithState() {
        var stateValue = "value";
        var headers = Map.of("Cookie", "Foo=bar; State=" + stateValue);
        var tokenRequestEvent = new TokenRequestEvent();
        tokenRequestEvent.setHeaders(headers);

        var cookieState = tokenRequestEvent.getCookieState();

        Assertions.assertThat(cookieState).isPresent();
        Assertions.assertThat(cookieState.get().getValue()).isEqualTo(stateValue);
    }

    @Test
    void getCookieStateReturnsEmptyWhenNoHeadersAreSet() {
        var tokenRequestEvent = new TokenRequestEvent();

        var cookieState = tokenRequestEvent.getCookieState();

        Assertions.assertThat(cookieState).isEmpty();
    }

    @Test
    void getCookieStateReturnsEmptyWhenNoCookieHeadersAreSet() {
        var tokenRequestEvent = new TokenRequestEvent();
        tokenRequestEvent.setHeaders(new HashMap<>());

        var cookieState = tokenRequestEvent.getCookieState();

        Assertions.assertThat(cookieState).isEmpty();
    }
}
