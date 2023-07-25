package uk.nhs.digital.docstore.authoriser.models;

import java.util.HashMap;
import java.util.List;

public class LoginEventResponse {
    private final Session session;
    private final HashMap<String, List<String>> usersOrgs;

    public LoginEventResponse(Session session, HashMap<String, List<String>> usersOrgs) {
        this.session = session;
        this.usersOrgs = usersOrgs;
    }

    public Session getSession() {
        return session;
    }

    public HashMap<String, List<String>> getUsersOrgs() {
        return usersOrgs;
    }
}
