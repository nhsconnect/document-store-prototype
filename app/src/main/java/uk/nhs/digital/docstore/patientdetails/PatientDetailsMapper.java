package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PatientDetailsMapper {
    public PatientDetails fromPatientDetailsResponseBody(String body) {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(body, PatientDetails.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
