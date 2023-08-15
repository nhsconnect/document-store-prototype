package uk.nhs.digital.docstore.authoriser.audit.message;

public class VerifyOrgRequestAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String sessionId;

    private final String description;

    public VerifyOrgRequestAuditMessage(String sessionId, String description) {
        super();
        this.sessionId = sessionId;
        this.description = description;
    }

    @SuppressWarnings("unused")
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
