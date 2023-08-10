package uk.nhs.digital.docstore.authoriser.audit.message;

public class StateAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String state;

    public StateAuditMessage(String state) {
        super();
        this.state = state;
    }

    @SuppressWarnings("unused")
    public String getState() {
        return state;
    }

    @Override
    public String getDescription() {
        return "New request to start login process received";
    }
}
