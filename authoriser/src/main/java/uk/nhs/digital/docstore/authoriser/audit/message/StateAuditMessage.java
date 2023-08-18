package uk.nhs.digital.docstore.authoriser.audit.message;

import lombok.Getter;

@Getter
public class StateAuditMessage extends BaseAuditMessage implements AuditMessage {

    private final String state;
    private final String description;

    public StateAuditMessage(String description, String state) {
        super();
        this.state = state;
        this.description = description;
    }

}
