package uk.nhs.digital.docstore.authoriser.audit.message;

public class UserInfoAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String userID;

    public UserInfoAuditMessage(String userID) {
        super();
        this.userID = userID;
    }

    @SuppressWarnings("unused")
    public String getUserID() {
        return userID;
    }

    @Override
    public String getDescription() {
        return "User logging in";
    }
}
