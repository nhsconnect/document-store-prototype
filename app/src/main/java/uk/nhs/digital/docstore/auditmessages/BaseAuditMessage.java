package uk.nhs.digital.docstore.auditmessages;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import uk.nhs.digital.docstore.config.Tracer;

import java.time.Instant;

public class BaseAuditMessage implements AuditMessage {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;
    private final String correlationId;

    public BaseAuditMessage() {
        this.correlationId = Tracer.getCorrelationId();
        this.timestamp = Instant.now();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String toJsonString() throws JsonProcessingException {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        return jsonMapper.writeValueAsString(this);
    }
}
