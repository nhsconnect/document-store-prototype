package uk.nhs.digital.docstore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.repository.DynamoDBSessionStore;
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
        var timeToExist = Instant.ofEpochSecond(1L);
        var session = Session.create(uuid, timeToExist, new Subject("sub"), new SessionID("sid"));

        db.save(session);

        var expected = dynamoDBMapper.load(Session.class, session.getPK(), session.getSK());
        assertEquals(session.getId(), expected.getId());
    }

    @Test
    public void shouldReadSessionsFromDynamoDB() {
        var uuid = UUID.randomUUID();
        var timeToExist = Instant.ofEpochSecond(1L);
        Subject subject = new Subject("sub");
        var session = Session.create(uuid, timeToExist, subject, new SessionID("sid"));

        dynamoDBMapper.save(session);
        var expected = db.load(subject, session.getId());

        assertTrue(expected.isPresent());
        assertEquals(session.getId(), expected.get().getId());
    }

    @Test
    public void shouldDeleteSessionFromDynamoDB() {
        var uuid = UUID.randomUUID();
        var timeToExist = Instant.ofEpochSecond(1L);
        var session = Session.create(uuid, timeToExist, new Subject("sub"), new SessionID("sid"));
        dynamoDBMapper.save(session);

        db.delete(session);

        var expectedEmpty =
                Optional.ofNullable(
                        dynamoDBMapper.load(Session.class, session.getPK(), session.getSK()));
        assertTrue(expectedEmpty.isEmpty());
    }

    @Test
    public void shouldLoadAllSessionsForASubjectFromDynamoDB() {
        var timeToExist = Instant.ofEpochSecond(1L);
        var subject = new Subject(UUID.randomUUID().toString());
        var oidcSessionIDOne = new SessionID("one");
        var oidcSessionIDTwo = new SessionID("two");

        var sessionOne = Session.create(UUID.randomUUID(), timeToExist, subject, oidcSessionIDOne);
        var sessionTwo = Session.create(UUID.randomUUID(), timeToExist, subject, oidcSessionIDTwo);
        dynamoDBMapper.save(sessionOne);
        dynamoDBMapper.save(sessionTwo);

        var results = db.queryByOIDCSubject(subject);
        var sessionIds = results.stream().map(Session::getId).collect(Collectors.toList());

        Assertions.assertThat(sessionIds).contains(sessionOne.getId(), sessionTwo.getId());
    }

    @Test
    public void shouldBatchDeleteSessionsFromDynamoDB() {
        var timeToExist = Instant.ofEpochSecond(1L);
        var subject = new Subject(UUID.randomUUID().toString());
        var oidcSessionIDOne = new SessionID("one");
        var oidcSessionIDTwo = new SessionID("two");

        var sessionOne = Session.create(UUID.randomUUID(), timeToExist, subject, oidcSessionIDOne);
        var sessionTwo = Session.create(UUID.randomUUID(), timeToExist, subject, oidcSessionIDTwo);
        dynamoDBMapper.save(sessionOne);
        dynamoDBMapper.save(sessionTwo);

        db.batchDelete(List.of(sessionOne, sessionTwo));

        Assertions.assertThat(
                        dynamoDBMapper.load(Session.class, sessionOne.getPK(), sessionOne.getSK()))
                .isNull();
        Assertions.assertThat(
                        dynamoDBMapper.load(Session.class, sessionTwo.getPK(), sessionTwo.getSK()))
                .isNull();
    }
}
