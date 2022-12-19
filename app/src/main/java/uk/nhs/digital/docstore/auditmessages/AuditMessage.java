package uk.nhs.digital.docstore.auditmessages;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuditMessage {
    String toJsonString() throws JsonProcessingException;
}
