package uk.nhs.digital.docstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.DynamoDBSessionStore;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.helpers.AWSServiceContainer;
import uk.nhs.digital.docstore.helpers.DynamoDBHelper;

public class DynamoDBSessionStoreTest {
    private final AWSServiceContainer aws = new AWSServiceContainer();
    private final DynamoDBHelper dynamoDBHelper = new DynamoDBHelper(aws.getDynamoDBClient());
    private final DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(aws.getDynamoDBClient());
    private final DynamoDBSessionStore db = new DynamoDBSessionStore(dynamoDBMapper);

    @BeforeEach
    public void cleanUp() {
        String tableName = "ARFAuth";
        dynamoDBHelper.refreshTable(tableName);
    }

    @Test
    public void shouldPersistSessionsToDynamoDB() {
        var uuid = UUID.randomUUID();
        var timeToExist = 1L;
        var session = Session.create(uuid, timeToExist);

        db.save(session);

        var expected = dynamoDBMapper.load(Session.class, session.getPK(), session.getSK());
        assertEquals(session.getId(), expected.getId());
    }

    @Test
    public void shouldReadSessionsFromDynamoDB() {
        var uuid = UUID.randomUUID();
        var timeToExist = 1L;
        var session = Session.create(uuid, timeToExist);

        dynamoDBMapper.save(session);

        var expected = db.load(session.getId());

        assertTrue(expected.isPresent());
        assertEquals(session.getId(), expected.get().getId());
    }

    @Test
    public void shouldDeleteSessionFromDynamoDB() {
        var uuid = UUID.randomUUID();
        var timeToExist = 1L;
        var session = Session.create(uuid, timeToExist);
        dynamoDBMapper.save(session);

        db.delete(session);

        var expectedEmpty =
                Optional.ofNullable(
                        dynamoDBMapper.load(Session.class, session.getPK(), session.getSK()));
        assertTrue(expectedEmpty.isEmpty());
    }
}
