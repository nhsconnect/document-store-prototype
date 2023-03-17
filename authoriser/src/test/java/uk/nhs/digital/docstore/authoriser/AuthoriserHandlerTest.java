package uk.nhs.digital.docstore.authoriser;

import java.util.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.requests.AuthoriserRequestEvent;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AuthoriserHandlerTest {
    @Test
    void respondsWithIamDenyPolicy() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        policyDocumentBuilder.withStatement(List.of(IamPolicyResponse.denyStatement("*")));
        var policyDocument = policyDocumentBuilder.build();
        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(policyDocument);

        var handler =  new AuthoriserHandler();
        var actual = handler.handleRequest(new AuthoriserRequestEvent(), Mockito.mock(Context.class));

        assertThat(actual.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }

    @Test
    void respondsWithIamAllowPolicyWhenSessionIsPresent() {
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        policyDocumentBuilder.withStatement(List.of(IamPolicyResponse.allowStatement("*")));
        var policyDocument = policyDocumentBuilder.build();
        var expectedResponse = new IamPolicyResponse();
        expectedResponse.setPolicyDocument(policyDocument);
        var request = new AuthoriserRequestEvent();
        var sessionId = UUID.randomUUID();
        request.setHeaders(Map.of("cookie", "SessionId=" + sessionId + ";"));

        var handler =  new AuthoriserHandler();
        var actual = handler.handleRequest(request, Mockito.mock(Context.class));

        assertThat(actual.getPolicyDocument()).usingRecursiveComparison().isEqualTo(expectedResponse.getPolicyDocument());
    }
}
