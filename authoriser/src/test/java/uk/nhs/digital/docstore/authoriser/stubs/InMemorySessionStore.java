package uk.nhs.digital.docstore.authoriser.stubs;

import com.nimbusds.oauth2.sdk.id.Subject;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import uk.nhs.digital.docstore.authoriser.SessionStore;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class InMemorySessionStore implements SessionStore {

    private final HashMap<String, Session> sessionHashMap;

    public InMemorySessionStore() {
        this.sessionHashMap = new HashMap<>();
    }

    @Override
    public void save(Session session) {
        this.sessionHashMap.put(session.getPK() + session.getSK(), session);
    }

    @Override
    public Optional<Session> load(Subject subject, UUID sessionID) {
        return Optional.ofNullable(
                sessionHashMap.get(
                        Session.PARTITION_KEY_PREFIX
                                + subject.getValue()
                                + Session.SORT_KEY_PREFIX
                                + sessionID));
    }

    @Override
    public void delete(Session session) {
        sessionHashMap.remove(session.getPK() + session.getSK());
    }

    @Override
    public void batchDelete(List<Session> sessions) {
        sessions.forEach(
                session -> {
                    sessionHashMap.remove(session.getPK() + session.getSK());
                });
    }

    @Override
    public List<Session> queryByOIDCSubject(Subject subject) {
        return sessionHashMap.values().stream()
                .filter(session -> session.getOIDCSubject().equals(subject.getValue()))
                .collect(Collectors.toList());
    }
}
