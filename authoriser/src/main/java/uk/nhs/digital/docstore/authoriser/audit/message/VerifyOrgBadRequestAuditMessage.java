package uk.nhs.digital.docstore.authoriser.audit.message;

public class VerifyOrgBadRequestAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String missingItemName;

    public VerifyOrgBadRequestAuditMessage(String missingItemName) {
        super();
        this.missingItemName = missingItemName;
    }

    @SuppressWarnings("unused")
    public String getMissingItemName() {
        return missingItemName;
    }

    @Override
    public String getDescription() {
        return String.format("Missing required item %s in verify org request", missingItemName);
    }
}
