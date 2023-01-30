package uk.nhs.digital.docstore.audit.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.Instant;
import uk.nhs.digital.docstore.config.Tracer;

public abstract class BaseAuditMessage {
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

    @SuppressWarnings("unused")
    public String getCorrelationId() {
        return correlationId;
    }

    public String toJsonString() throws JsonProcessingException {
        var jsonMapper = JsonMapper.builder().findAndAddModules().build();
        return jsonMapper.writeValueAsString(this);
    }
}
