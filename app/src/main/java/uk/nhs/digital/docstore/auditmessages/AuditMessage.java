package uk.nhs.digital.docstore.auditmessages;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.Instant;

public interface AuditMessage {
    Instant getTimestamp();

    @SuppressWarnings("unused")
    String getCorrelationId();

    String toJsonString() throws JsonProcessingException;
}
