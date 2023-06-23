package uk.nhs.digital.docstore.authoriser.models;

import uk.nhs.digital.docstore.authoriser.enums.LoginEventOutcome;

public class LoginEventResponse {
    private final Session session;
    private final LoginEventOutcome outcome;

    public LoginEventResponse(Session session, LoginEventOutcome loginEventOutcome) {
        this.session = session;
        this.outcome = loginEventOutcome;
    }

    public Session getSession() {
        return session;
    }

    public LoginEventOutcome getOutcome() {
        return outcome;
    }
}
