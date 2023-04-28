package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.nhs.digital.docstore.authoriser.handlers.LogoutHandler;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.LogoutRequestEvent;
import uk.nhs.digital.docstore.helpers.AWSServiceContainer;
import uk.nhs.digital.docstore.helpers.DynamoDBHelper;


public class LogoutTest {
    @Mock private Context context;
    private final AWSServiceContainer aws = new AWSServiceContainer();
    private final DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(aws.getDynamoDBClient());
    private final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(aws.getDynamoDBClient());
    private final DynamoDBSessionStore db = new DynamoDBSessionStore(dynamoDBMapper);

    @BeforeEach
    public void setUp() {
        String tableName = "ARFAuth";
        dynamoDBHelper.refreshTable(tableName);
    }

    @Test
    public void shouldRemoveSessionByIdAndSubject() {
        var subjectToRemove = new Subject("subToRemove");
        var sessionIdToRemove = new SessionID("sidToRemove");
        var timeToExist = Instant.ofEpochSecond(1L);
        var sessionOne = Session.create(UUID.randomUUID(), timeToExist, subjectToRemove, sessionIdToRemove);
        var sessionTwo = Session.create(UUID.randomUUID(), timeToExist, new Subject("sub2"), new SessionID("sid2"));
        db.save(sessionOne);
        db.save(sessionTwo);
        var request = createRequestEvent(sessionIdToRemove, subjectToRemove);

        var logoutHandler = new LogoutHandler(db);
        /*var result =*/ logoutHandler.handleRequest(request, context);

        assert(db.load(subjectToRemove, UUID.fromString(sessionIdToRemove.getValue())).isEmpty());
    }

    @Test
    public void shouldRedirectToRequestedUrl() {}

    private LogoutRequestEvent createRequestEvent(SessionID sessionId, Subject subject) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cookie", "SessionId" + sessionId.getValue() + "; SubjectClaim" + subject.getValue());

        return new LogoutRequestEvent().withHeaders(headers);
    }
}
