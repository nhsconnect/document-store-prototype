package uk.nhs.digital.docstore.auditmessages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.time.Instant;

public class BaseAuditMessage implements AuditMessage {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;

    public BaseAuditMessage() {
        this.timestamp = Instant.now();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String toJsonString() throws JsonProcessingException {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        return jsonMapper.writeValueAsString(this);
    }
}
