package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.model.NhsNumber;

public abstract class PatientRelatedAuditMessage extends BaseAuditMessage {
    private final NhsNumber nhsNumber;

    public PatientRelatedAuditMessage(NhsNumber nhsNumber) {
        super();
        this.nhsNumber = nhsNumber;
    }

    public NhsNumber getNhsNumber() {
        return nhsNumber;
    }
}
