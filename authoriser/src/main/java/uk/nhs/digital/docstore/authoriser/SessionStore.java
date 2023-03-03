package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.id.Subject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import uk.nhs.digital.docstore.authoriser.models.Session;

public interface SessionStore {
    void save(Session session);

    Optional<Session> load(Subject subject, UUID sessionID);

    void delete(Session session);

    List<Session> queryByOIDCSubject(Subject subject);
}
