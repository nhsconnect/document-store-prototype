package uk.nhs.digital.docstore.audit.message;

import uk.nhs.digital.docstore.model.NhsNumber;

public class SearchPatientDetailsAuditMessage extends PatientRelatedAuditMessage
        implements AuditMessage {
    private final int pdsResponseStatus;

    public SearchPatientDetailsAuditMessage(NhsNumber nhsNumber, int pdsResponseStatus) {
        super(nhsNumber);
        this.pdsResponseStatus = pdsResponseStatus;
    }

    @SuppressWarnings("unused")
    public int getPdsResponseStatus() {
        return pdsResponseStatus;
    }

    @Override
    public String getDescription() {
        return "Searched for patient details";
    }
}
