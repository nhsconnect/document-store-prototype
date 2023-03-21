package uk.nhs.digital.docstore.authoriser.requests;

import com.nimbusds.oauth2.sdk.id.Subject;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AuthoriserRequestEventTest {
    @Test
    void getsSubjectCookieFromHeaders() {
        var requestEventSubject = new Subject("some-subject");
        var headers =
                Map.of(
                        "cookie",
                        "SessionId=some-session-id; SubjectClaim="
                                + requestEventSubject.getValue());
        var authoriserRequestEvent = new AuthoriserRequestEvent();
        authoriserRequestEvent.setHeaders(headers);

        var subject = authoriserRequestEvent.getSubject();

        Assertions.assertThat(subject).isPresent();
        Assertions.assertThat(subject.get().getValue()).isEqualTo(requestEventSubject.getValue());
    }

    @Test
    void returnsNullableIfNoSubjectCookieFound() {
        var headers = Map.of("cookie", "SessionId=some-session-id;");
        var authoriserRequestEvent = new AuthoriserRequestEvent();
        authoriserRequestEvent.setHeaders(headers);

        var subject = authoriserRequestEvent.getSubject();

        Assertions.assertThat(subject).isNotPresent();
    }

    @Test
    void getsSessionIdCookieFromHeaders() {
        var requestEventSessionId = UUID.randomUUID();
        var headers =
                Map.of(
                        "cookie",
                        "SessionId=" + requestEventSessionId + "; SubjectClaim=some-subject;");
        var authoriserRequestEvent = new AuthoriserRequestEvent();
        authoriserRequestEvent.setHeaders(headers);

        var sessionId = authoriserRequestEvent.getSessionId();

        Assertions.assertThat(sessionId).isPresent();
        Assertions.assertThat(sessionId.get()).isEqualTo(requestEventSessionId);
    }

    @Test
    void returnsNullableIfNoSessionIdCookieFound() {
        var headers = Map.of("cookie", "SubjectClaim=some-subject;");
        var authoriserRequestEvent = new AuthoriserRequestEvent();
        authoriserRequestEvent.setHeaders(headers);

        var sessionId = authoriserRequestEvent.getSessionId();

        Assertions.assertThat(sessionId).isNotPresent();
    }

    @Test
    void returnsNullableIfThereAreNoHeaders() {
        var authoriserRequestEvent = new AuthoriserRequestEvent();

        var subject = authoriserRequestEvent.getSubject();
        var sessionId = authoriserRequestEvent.getSessionId();

        Assertions.assertThat(subject).isNotPresent();
        Assertions.assertThat(sessionId).isNotPresent();
    }

    @Test
    void returnsNullableIfThereAreNoCookies() {
        var headers = Map.of("some-header", "some-header-value");
        var authoriserRequestEvent = new AuthoriserRequestEvent();
        authoriserRequestEvent.setHeaders(headers);

        var subject = authoriserRequestEvent.getSubject();
        var sessionId = authoriserRequestEvent.getSessionId();

        Assertions.assertThat(subject).isNotPresent();
        Assertions.assertThat(sessionId).isNotPresent();
    }

    @Test
    void returnsNullableIfCookiesAreEmpty() {
        var headers = Map.of("cookie", "");
        var authoriserRequestEvent = new AuthoriserRequestEvent();
        authoriserRequestEvent.setHeaders(headers);

        var subject = authoriserRequestEvent.getSubject();
        var sessionId = authoriserRequestEvent.getSessionId();

        Assertions.assertThat(subject).isNotPresent();
        Assertions.assertThat(sessionId).isNotPresent();
    }
}
