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
            AuthoriserRequestEvent request, Context context) {
        IamPolicyResponse.Statement statement;

        if(request.getSessionId().isPresent()) {
            statement = IamPolicyResponse.allowStatement("*");
        } else {
            statement = IamPolicyResponse.denyStatement("*");
        }

        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        policyDocumentBuilder.withStatement(List.of(statement));
        var policyDocument = policyDocumentBuilder.build();
        var response = new IamPolicyResponse();
        response.setPolicyDocument(policyDocument);

        return response;
    }
}
