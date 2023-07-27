package uk.nhs.digital.docstore.authoriser.audit.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.authoriser.audit.message.AuditMessage;

public interface AuditPublisher {
    void publish(AuditMessage auditMessage) throws JsonProcessingException;
}
