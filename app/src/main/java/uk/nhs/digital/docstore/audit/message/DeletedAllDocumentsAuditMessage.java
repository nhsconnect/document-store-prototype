package uk.nhs.digital.docstore.audit.message;

public class DeletedAllDocumentsAuditMessage extends BaseAuditMessage implements AuditMessage {
    @Override
    public String getDescription() {
        return "Deleted all documents";
    }
}
