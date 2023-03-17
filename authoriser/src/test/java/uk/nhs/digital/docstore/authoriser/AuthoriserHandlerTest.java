package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.requests.AuthoriserRequestEvent;

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
        var authoriserHandler = new AuthoriserHandler();

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
        var authoriserHandler = new AuthoriserHandler();

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
        var authoriserHandler = new AuthoriserHandler();

        var actualIamPolicyResponse =
                authoriserHandler.handleRequest(requestEvent, mock(Context.class));

        assertThat(actualIamPolicyResponse.getPolicyDocument())
                .usingRecursiveComparison()
                .isEqualTo(expectedIamPolicyResponse.getPolicyDocument());
    }

    @Test
    void respondsWithIamAllowPolicyWhenSessionIdAndSubjectCookieIsPresent() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var policyDocument =
                policyDocumentBuilder
                        .withStatement(List.of(IamPolicyResponse.allowStatement("*")))
                        .build();
        var expectedIamPolicyResponse = new IamPolicyResponse();
        expectedIamPolicyResponse.setPolicyDocument(policyDocument);
        var requestEvent = new AuthoriserRequestEvent();
        requestEvent.setHeaders(
                Map.of(
                        "cookie",
                        "SessionId=" + UUID.randomUUID() + "; SubjectClaim=some-subject;"));
        var authoriserHandler = new AuthoriserHandler();

        var actualIamPolicyResponse =
                authoriserHandler.handleRequest(requestEvent, mock(Context.class));

        assertThat(actualIamPolicyResponse.getPolicyDocument())
                .usingRecursiveComparison()
                .isEqualTo(expectedIamPolicyResponse.getPolicyDocument());
    }
}
