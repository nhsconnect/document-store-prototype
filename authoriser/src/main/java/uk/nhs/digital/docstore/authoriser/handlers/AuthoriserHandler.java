package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.AuthoriserRequestEvent;

public class AuthoriserHandler extends BaseAuthRequestHandler
        implements RequestHandler<AuthoriserRequestEvent, IamPolicyResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoriserHandler.class);

    private final SessionStore sessionStore;

    @SuppressWarnings("unused")
    public AuthoriserHandler() {
        this(new DynamoDBSessionStore(new DynamoDBMapper(getDynamodbClient())));
    }

    public AuthoriserHandler(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public IamPolicyResponse handleRequest(AuthoriserRequestEvent requestEvent, Context context) {
        var sessionId = requestEvent.getSessionId();
        var subject = requestEvent.getSubject();
        var policyDocumentBuilder = IamPolicyResponse.PolicyDocument.builder();
        var iamPolicyResponse = new IamPolicyResponse();

        // TODO: [PRMT-2779] Add identifier such as a redacted session ID
        LOGGER.debug("Handling authorisation request:" + requestEvent);

        if (sessionId.isPresent() && subject.isPresent()) {
            // TODO: [PRMT-2779] Remove/improve this redaction if it is insufficient
            var redactedSessionId =
                    sessionId.get().toString().substring(sessionId.get().toString().length() - 4);
            LOGGER.debug("Retrieving session for session ID ending in: " + redactedSessionId);

            var session = sessionStore.load(subject.get(), sessionId.get());

            if (session.isPresent()) {
                LOGGER.debug(
                        "Attaching allow statement to IAM policy for session ID ending in: "
                                + redactedSessionId);

                var allowStatement = IamPolicyResponse.allowStatement("*");
                var policyDocument =
                        policyDocumentBuilder.withStatement(List.of(allowStatement)).build();

                iamPolicyResponse.setPolicyDocument(policyDocument);

                return iamPolicyResponse;
            }

            LOGGER.debug("Unable to find session for session ID ending in: " + redactedSessionId);
        }

        // TODO: [PRMT-2779] Add identifier such as a redacted session ID
        LOGGER.debug("Attaching deny statement to IAM policy");

        var denyStatement = IamPolicyResponse.denyStatement("*");
        var policyDocument = policyDocumentBuilder.withStatement(List.of(denyStatement)).build();

        iamPolicyResponse.setPolicyDocument(policyDocument);

        return iamPolicyResponse;
    }
}
