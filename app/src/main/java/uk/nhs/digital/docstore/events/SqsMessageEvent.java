package uk.nhs.digital.docstore.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SqsMessageEvent {
    private final ReRegistrationEvent message;

    public SqsMessageEvent(@JsonProperty("Message") ReRegistrationEvent message) {
        this.message = message;
    }

    public ReRegistrationEvent getMessage() {
        return message;
    }

    public static SqsMessageEvent parse(String message) {
        var objectMapper = JsonMapper.builder().build();
        try {
            return objectMapper.readValue(message, SqsMessageEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
