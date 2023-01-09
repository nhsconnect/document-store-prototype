package uk.nhs.digital.docstore.audit.message;

public class SearchPatientDetailsAuditMessage extends BaseAuditMessage {
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
}
