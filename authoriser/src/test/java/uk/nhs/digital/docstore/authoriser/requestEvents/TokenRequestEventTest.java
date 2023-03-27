package uk.nhs.digital.docstore.authoriser.requestEvents;

import com.nimbusds.oauth2.sdk.id.State;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenRequestEventTest {

    @Test
    void getCookieStateParsesSingleValueCookieHeader() {
        var tokenRequestEvent = new TokenRequestEvent();

        String stateValue = "value";
        var headers = Map.of("Cookie", "State=" + stateValue);

        tokenRequestEvent.setHeaders(headers);

        Optional<State> result = tokenRequestEvent.getCookieState();
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getValue()).isEqualTo(stateValue);
    }

    @Test
    void getCookieStateParsesMultiValueCookieHeader() {
        var tokenRequestEvent = new TokenRequestEvent();

        String stateValue = "value";
        var headers = Map.of("Cookie", "Foo=bar; State=" + stateValue);

        tokenRequestEvent.setHeaders(headers);

        var result = tokenRequestEvent.getCookieState();
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getValue()).isEqualTo(stateValue);
    }

    @Test
    void getCookieStateReturnsEmptyWhenNoHeadersAreSet() {
        var tokenRequestEvent = new TokenRequestEvent();

        var result = tokenRequestEvent.getCookieState();

        Assertions.assertThat(result).isEmpty();
    }

    @Test
    void getCookieStateReturnsEmptyWhenNoCookieHeadersAreSet() {
        var tokenRequestEvent = new TokenRequestEvent();
        tokenRequestEvent.setHeaders(new HashMap<>());
        tokenRequestEvent.setMultiValueHeaders(new HashMap<>());

        var result = tokenRequestEvent.getCookieState();
        Assertions.assertThat(result).isEmpty();
    }
}
