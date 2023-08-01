package uk.nhs.digital.docstore.authoriser.repository;

import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import uk.nhs.digital.docstore.authoriser.models.Session;

public interface SessionStore {
    void save(Session session);

    Optional<Session> load(Subject subject, UUID sessionID);

    void delete(Session session);

    void batchDelete(List<Session> sessions);

    List<Session> queryByOIDCSubject(Subject subject);

    List<Session> queryBySessionId(SessionID sessionId);

    Optional<Session> queryBySessionIdWithKeys(String subjectClaim, String sessionId);
}
