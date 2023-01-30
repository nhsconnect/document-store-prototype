package uk.nhs.digital.docstore.audit.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.audit.message.AuditMessage;

public interface AuditPublisher {
    void publish(AuditMessage auditMessage) throws JsonProcessingException;
}
