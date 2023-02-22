package uk.nhs.digital.docstore.authoriser;

import java.util.Optional;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class DynamoDBSessionStore implements SessionStore {
    @Override
    public void save(Session session) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Optional<Session> load(String sessionID) {
        return Optional.empty();
    }
}
