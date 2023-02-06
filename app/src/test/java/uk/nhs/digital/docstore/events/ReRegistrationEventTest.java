package uk.nhs.digital.docstore.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

public class ReRegistrationEventTest {

    @Test
    void parsesReRegistrationFromString()
            throws IllFormedPatientDetailsException, JsonProcessingException {
        var nhsNumber = "9876543210";
        var nemsMessageId = "123";

        var reRegistrationMessage =
                new JSONObject()
                        .put("nhsNumber", nhsNumber)
                        .put("newlyRegisteredOdsCode", "ABC12")
                        .put("nemsMessageId", nemsMessageId)
                        .put("lastUpdated", "some date")
                        .toString();

        var expectedReRegistrationEvent = new ReRegistrationEvent(nhsNumber, nemsMessageId);
        var reRegistrationEvent = ReRegistrationEvent.parse(reRegistrationMessage);

        assertEquals(expectedReRegistrationEvent, reRegistrationEvent);
    }
}
