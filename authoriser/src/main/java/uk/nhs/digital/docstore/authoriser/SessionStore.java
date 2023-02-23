package uk.nhs.digital.docstore.authoriser;

import java.util.Optional;
import java.util.UUID;
import uk.nhs.digital.docstore.authoriser.models.Session;

public interface SessionStore {
    void save(Session session);

    Optional<Session> load(UUID sessionID);
}
