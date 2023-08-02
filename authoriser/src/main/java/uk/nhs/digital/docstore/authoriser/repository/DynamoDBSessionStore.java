package uk.nhs.digital.docstore.authoriser.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import java.util.*;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class DynamoDBSessionStore implements SessionStore {
    private final DynamoDBMapper dynamoDBMapper;

    public DynamoDBSessionStore(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public static final String PARTITION_KEY_PREFIX = "OIDCSUBJECT#";
    public static final String SORT_KEY_PREFIX = "SESSION#";

    @Override
    public void save(Session session) {
        dynamoDBMapper.save(session);
    }

    @Override
    public Optional<Session> load(Subject subject, UUID sessionID) {
        var partitionKey = Session.PARTITION_KEY_PREFIX + subject.getValue();
        var sortKey = Session.SORT_KEY_PREFIX + sessionID;

        var session = dynamoDBMapper.load(Session.class, partitionKey, sortKey);

        return Optional.ofNullable(session);
    }

    @Override
    public void delete(Session session) {
        dynamoDBMapper.delete(session);
    }

    @Override
    public void batchDelete(List<Session> sessions) {
        dynamoDBMapper.batchDelete(sessions);
    }

    @Override
    public PaginatedQueryList<Session> queryByOIDCSubject(Subject subject) {
        var partitionKey = Session.PARTITION_KEY_PREFIX + subject.getValue();
        var expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":pk", new AttributeValue().withS(partitionKey));
        var queryExpression =
                new DynamoDBQueryExpression<Session>()
                        .withKeyConditionExpression("PK = :pk")
                        .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.query(Session.class, queryExpression);
    }

    @Override
    public List<Session> queryBySessionId(SessionID sessionId) {
        var expressionAttributeValues = new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":id", new AttributeValue().withS(sessionId.getValue()));
        var queryExpression =
                new DynamoDBQueryExpression<Session>()
                        .withKeyConditionExpression("oidcSessionId = :id")
                        .withExpressionAttributeValues(expressionAttributeValues);

        return dynamoDBMapper.query(Session.class, queryExpression);
    }
}
