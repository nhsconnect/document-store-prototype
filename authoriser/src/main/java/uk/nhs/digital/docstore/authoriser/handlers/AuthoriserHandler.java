package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.List;
import uk.nhs.digital.docstore.authoriser.SessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.AuthoriserRequestEvent;

public class AuthoriserHandler
        implements RequestHandler<AuthoriserRequestEvent, IamPolicyResponse> {
    private final SessionStore sessionStore;

    public AuthoriserHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public IamPolicyResponse handleRequest(AuthoriserRequestEvent requestEvent, Context context) {
        var sessionId = requestEvent.getSessionId();
        var subject = requestEvent.getSubject();
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var iamPolicyResponse = new IamPolicyResponse();

        if (sessionId.isPresent() && subject.isPresent()) {
            var session = sessionStore.load(subject.get(), sessionId.get());

            if (session.isPresent()) {
                var statement = IamPolicyResponse.allowStatement("*");
                var policyDocument =
                        policyDocumentBuilder.withStatement(List.of(statement)).build();

                iamPolicyResponse.setPolicyDocument(policyDocument);

                return iamPolicyResponse;
            }
        }

        var statement = IamPolicyResponse.denyStatement("*");
        var policyDocument = policyDocumentBuilder.withStatement(List.of(statement)).build();

        iamPolicyResponse.setPolicyDocument(policyDocument);

        return iamPolicyResponse;
    }
}
