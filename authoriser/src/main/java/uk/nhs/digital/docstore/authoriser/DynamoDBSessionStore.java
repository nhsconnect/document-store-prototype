package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import java.util.Optional;
import java.util.UUID;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class DynamoDBSessionStore implements SessionStore {
    private final DynamoDBMapper dynamoDBMapper;

    public DynamoDBSessionStore(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public void save(Session session) {
        dynamoDBMapper.save(session);
    }

    @Override
    public Optional<Session> load(UUID sessionID) {
        var key = Session.KEY_PREFIX + sessionID;

        var session = dynamoDBMapper.load(Session.class, key, key);
        return Optional.ofNullable(session);
    }
}
