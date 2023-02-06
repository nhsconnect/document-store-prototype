package uk.nhs.digital.docstore.events;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

class SqsMessageEventTest {

    @Test
    void parsesReRegistrationFromString() throws IllFormedPatientDetailsException, JsonProcessingException {
        var nhsNumber = "9876543210";
        var nemsMessageId = "123";

        var reRegistrationMessage =
                new JSONObject()
                        .put("nhsNumber", nhsNumber)
                        .put("newlyRegisteredOdsCode", "ABC12")
                        .put("nemsMessageId", nemsMessageId)
                        .put("lastUpdated", "some date");
        var message = new JSONObject().put("Message", reRegistrationMessage).toString();

        var reRegistrationEvent = new ReRegistrationEvent(nhsNumber, nemsMessageId);
        var expectedSqsMessageEvent = new SqsMessageEvent(reRegistrationEvent);

        var sqsMessageEvent = SqsMessageEvent.parse(message);

        assertEquals(expectedSqsMessageEvent, sqsMessageEvent);
    }
}
