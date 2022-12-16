package uk.nhs.digital.docstore.auditmessages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PatientSearchAuditMessage {
    private final String nhsNumber;
    private final PdsResponseStatusType pdsResponseStatus;
    private final String dateTime;


    public PatientSearchAuditMessage(String nhsNumber, PdsResponseStatusType pdsResponseStatus, String dateTime) {
        this.nhsNumber = nhsNumber;
        this.pdsResponseStatus = pdsResponseStatus;
        this.dateTime = dateTime;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public PdsResponseStatusType getPdsResponseStatus() {
        return pdsResponseStatus;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String toJsonString() throws JsonProcessingException {
        var ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(this);

    }
}
