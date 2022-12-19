package uk.nhs.digital.docstore.auditmessages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class BaseAuditMessage  implements  AuditMessage {
    public String toJsonString() throws JsonProcessingException {
        var ow = JsonMapper.builder()
                .findAndAddModules()
                .build();
        return ow.writeValueAsString(this);
    }
}
