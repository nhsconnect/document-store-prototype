package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.SessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.AuthoriserRequestEvent;

public class AuthoriserHandler
        implements RequestHandler<AuthoriserRequestEvent, IamPolicyResponse> {
    private final SessionStore sessionStore;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoriserHandler.class);

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
            // TODO redact session info
            LOGGER.debug(
                    "Auth lambda invoked for user with sessionID"
                            + sessionId
                            + " and subjectClaim "
                            + subject);
            var session = sessionStore.load(subject.get(), sessionId.get());

            if (session.isPresent()) {
                LOGGER.debug(
                        "Session found for user with sessionID"
                                + sessionId
                                + " and subjectClaim "
                                + subject);
                var allowStatement = IamPolicyResponse.allowStatement("*");
                var policyDocument =
                        policyDocumentBuilder.withStatement(List.of(allowStatement)).build();

                iamPolicyResponse.setPolicyDocument(policyDocument);

                return iamPolicyResponse;
            }

            LOGGER.debug(
                    "Session not found for user with sessionID"
                            + sessionId
                            + " and subjectClaim "
                            + subject);
        }
        LOGGER.debug("Auth lambda invoked for unknown requester");
        var denyStatement = IamPolicyResponse.denyStatement("*");
        var policyDocument = policyDocumentBuilder.withStatement(List.of(denyStatement)).build();

        iamPolicyResponse.setPolicyDocument(policyDocument);

        return iamPolicyResponse;
    }
}
