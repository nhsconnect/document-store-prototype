package uk.nhs.digital.docstore.authoriser.audit.message;

public class StateAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String state;
    private final String description;

    public StateAuditMessage(String description, String state) {
        super();
        this.state = state;
        this.description = description;
    }

    @SuppressWarnings("unused")
    public String getState() {
        return state;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
