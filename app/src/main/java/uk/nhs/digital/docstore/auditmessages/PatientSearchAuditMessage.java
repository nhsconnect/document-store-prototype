package uk.nhs.digital.docstore.auditmessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.time.Instant;

public class PatientSearchAuditMessage implements AuditMessage{
    private final String nhsNumber;
    private final int pdsResponseStatus;
    private final Instant dateTime;


    public PatientSearchAuditMessage(String nhsNumber, int pdsResponseStatus, Instant dateTime) {
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

    public String toJsonString() throws JsonProcessingException {
        var ow = JsonMapper.builder()
                .findAndAddModules()
                .build();
        return ow.writeValueAsString(this);
    }
}
