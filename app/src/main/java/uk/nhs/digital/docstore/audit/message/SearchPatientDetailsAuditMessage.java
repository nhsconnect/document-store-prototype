package uk.nhs.digital.docstore.audit.message;

public class SearchPatientDetailsAuditMessage extends BaseAuditMessage implements AuditMessage {
    private final String nhsNumber;
    private final int pdsResponseStatus;

    public SearchPatientDetailsAuditMessage(String nhsNumber, int pdsResponseStatus) {
        this.nhsNumber = nhsNumber;
        this.pdsResponseStatus = pdsResponseStatus;
    }

    public String getNhsNumber() {
        return nhsNumber;
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
