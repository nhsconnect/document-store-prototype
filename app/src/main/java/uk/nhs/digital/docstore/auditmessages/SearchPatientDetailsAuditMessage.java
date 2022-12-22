package uk.nhs.digital.docstore.auditmessages;

import java.time.Instant;

public class SearchPatientDetailsAuditMessage extends BaseAuditMessage {
    private final String nhsNumber;
    private final int pdsResponseStatus;
    private final Instant dateTime;


    public SearchPatientDetailsAuditMessage(String nhsNumber, int pdsResponseStatus, Instant dateTime) {
        this.nhsNumber = nhsNumber;
        this.pdsResponseStatus = pdsResponseStatus;
        this.dateTime = dateTime;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public int getPdsResponseStatus() {
        return pdsResponseStatus;
    }

    public Instant getDateTime() {
        return dateTime;
    }
}
