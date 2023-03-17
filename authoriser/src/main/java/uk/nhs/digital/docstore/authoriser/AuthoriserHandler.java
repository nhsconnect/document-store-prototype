package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import uk.nhs.digital.docstore.authoriser.requests.AuthoriserRequestEvent;

import java.util.List;

public class AuthoriserHandler
        implements RequestHandler<AuthoriserRequestEvent, IamPolicyResponse> {

    @Override
    public IamPolicyResponse handleRequest(
            AuthoriserRequestEvent requestEvent, Context context) {
        IamPolicyResponse.Statement statement;

        if (requestEvent.getSessionId().isPresent()) {
            statement = IamPolicyResponse.allowStatement("*");
        } else {
            statement = IamPolicyResponse.denyStatement("*");
        }

        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var policyDocument = policyDocumentBuilder.withStatement(List.of(statement)).build();

        var iamPolicyResponse = new IamPolicyResponse();
        iamPolicyResponse.setPolicyDocument(policyDocument);

        return iamPolicyResponse;
    }
}
