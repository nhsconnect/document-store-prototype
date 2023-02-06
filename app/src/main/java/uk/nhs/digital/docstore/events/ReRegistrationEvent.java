package uk.nhs.digital.docstore.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReRegistrationEvent {
    private final NhsNumber nhsNumber;
    private final String nemsMessageId;

    public ReRegistrationEvent(
            @JsonProperty("nhsNumber") String nhsNumber,
            @JsonProperty("nemsMessageId") String nemsMessageId)
            throws IllFormedPatientDetailsException {
        this.nhsNumber = new NhsNumber(nhsNumber);
        this.nemsMessageId = nemsMessageId;
    }

    public static ReRegistrationEvent parse(String message) throws JsonProcessingException {
        var objectMapper = JsonMapper.builder().build();
        return objectMapper.readValue(message, ReRegistrationEvent.class);
    }

    public NhsNumber getNhsNumber() {
        return nhsNumber;
    }

    public String getNemsMessageId() {
        return nemsMessageId;
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
