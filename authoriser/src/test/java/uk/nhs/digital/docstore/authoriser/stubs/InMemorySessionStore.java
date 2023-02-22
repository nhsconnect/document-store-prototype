package uk.nhs.digital.docstore.authoriser.stubs;

import java.util.HashMap;
import java.util.Optional;
import uk.nhs.digital.docstore.authoriser.SessionStore;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class InMemorySessionStore implements SessionStore {

    private final HashMap<String, Session> sessions;

    public InMemorySessionStore() {
        this.sessions = new HashMap<>();
    }

    @Override
    public void save(Session session) {
        this.sessions.put(session.getPK() + session.getSK(), session);
    }

    @Override
    public Optional<Session> load(String sessionID) {
        return Optional.ofNullable(sessions.get("SESSION#" + sessionID + "SESSION#" + sessionID));
    }
}
