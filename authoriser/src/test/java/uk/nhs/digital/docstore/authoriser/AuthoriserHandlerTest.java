package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.requests.AuthoriserRequestEvent;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class AuthoriserHandlerTest {
    @Test
    void respondsWithIamDenyPolicyWhenSessionIdAndSubjectAreNotPresent() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var policyDocument =
                policyDocumentBuilder
                        .withStatement(List.of(IamPolicyResponse.denyStatement("*")))
                        .build();
        var expectedIamPolicyResponse = new IamPolicyResponse();
        expectedIamPolicyResponse.setPolicyDocument(policyDocument);
        var authoriserHandler = new AuthoriserHandler(new InMemorySessionStore());

        var actualIamPolicyResponse =
                authoriserHandler.handleRequest(new AuthoriserRequestEvent(), mock(Context.class));

        assertThat(actualIamPolicyResponse.getPolicyDocument())
                .usingRecursiveComparison()
                .isEqualTo(expectedIamPolicyResponse.getPolicyDocument());
    }

    @Test
    void respondsWithIamDenyPolicyWhenSessionIdIsNotPresent() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var policyDocument =
                policyDocumentBuilder
                        .withStatement(List.of(IamPolicyResponse.denyStatement("*")))
                        .build();
        var expectedIamPolicyResponse = new IamPolicyResponse();
        expectedIamPolicyResponse.setPolicyDocument(policyDocument);
        var requestEvent = new AuthoriserRequestEvent();
        requestEvent.setHeaders(Map.of("cookie", "Subject=some-subject;"));
        var authoriserHandler = new AuthoriserHandler(new InMemorySessionStore());

        var actualIamPolicyResponse =
                authoriserHandler.handleRequest(requestEvent, mock(Context.class));

        assertThat(actualIamPolicyResponse.getPolicyDocument())
                .usingRecursiveComparison()
                .isEqualTo(expectedIamPolicyResponse.getPolicyDocument());
    }

    @Test
    void respondsWithIamDenyPolicyWhenSubjectIsNotPresent() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var policyDocument =
                policyDocumentBuilder
                        .withStatement(List.of(IamPolicyResponse.denyStatement("*")))
                        .build();
        var expectedIamPolicyResponse = new IamPolicyResponse();
        expectedIamPolicyResponse.setPolicyDocument(policyDocument);
        var requestEvent = new AuthoriserRequestEvent();
        requestEvent.setHeaders(Map.of("cookie", "SessionId=" + UUID.randomUUID() + ";"));
        var authoriserHandler = new AuthoriserHandler(new InMemorySessionStore());

        var actualIamPolicyResponse =
                authoriserHandler.handleRequest(requestEvent, mock(Context.class));

        assertThat(actualIamPolicyResponse.getPolicyDocument())
                .usingRecursiveComparison()
                .isEqualTo(expectedIamPolicyResponse.getPolicyDocument());
    }

    @Test
    void respondsWithIamAllowPolicyWhenSessionIsPresent() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var policyDocument =
                policyDocumentBuilder
                        .withStatement(List.of(IamPolicyResponse.allowStatement("*")))
                        .build();
        var expectedIamPolicyResponse = new IamPolicyResponse();
        expectedIamPolicyResponse.setPolicyDocument(policyDocument);
        var requestEvent = new AuthoriserRequestEvent();
        var sessionId = UUID.randomUUID();
        var subject = new Subject();
        requestEvent.setHeaders(
                Map.of("cookie", "SessionId=" + sessionId + "; SubjectClaim=" + subject + ";"));
        var inMemorySessionStore = new InMemorySessionStore();
        var authoriserHandler = new AuthoriserHandler(inMemorySessionStore);
        var session =
                Session.create(
                        sessionId,
                        Instant.ofEpochSecond(1L),
                        subject,
                        new SessionID("some-session-id"));

        inMemorySessionStore.save(session);
        var actualIamPolicyResponse =
                authoriserHandler.handleRequest(requestEvent, mock(Context.class));

        assertThat(actualIamPolicyResponse.getPolicyDocument())
                .usingRecursiveComparison()
                .isEqualTo(expectedIamPolicyResponse.getPolicyDocument());
    }

    @Test
    void respondsWithIamDenyPolicyWhenSessionIsNotPresent() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var policyDocument =
                policyDocumentBuilder
                        .withStatement(List.of(IamPolicyResponse.denyStatement("*")))
                        .build();
        var expectedIamPolicyResponse = new IamPolicyResponse();
        expectedIamPolicyResponse.setPolicyDocument(policyDocument);
        var requestEvent = new AuthoriserRequestEvent();
        requestEvent.setHeaders(
                Map.of(
                        "cookie",
                        "SessionId=" + UUID.randomUUID() + "; SubjectClaim=some-subject;"));
        var authoriserHandler = new AuthoriserHandler(new InMemorySessionStore());

        var actualIamPolicyResponse =
                authoriserHandler.handleRequest(requestEvent, mock(Context.class));

        assertThat(actualIamPolicyResponse.getPolicyDocument())
                .usingRecursiveComparison()
                .isEqualTo(expectedIamPolicyResponse.getPolicyDocument());
    }
}
