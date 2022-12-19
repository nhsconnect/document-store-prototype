package uk.nhs.digital.docstore.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.auditmessages.AuditMessage;

public interface AuditPublisher {
     void publish(AuditMessage auditMessage) throws JsonProcessingException;
}
