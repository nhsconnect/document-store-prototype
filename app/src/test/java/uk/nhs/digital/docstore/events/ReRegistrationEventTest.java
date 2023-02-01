package uk.nhs.digital.docstore.events;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

import static org.junit.jupiter.api.Assertions.*;

class ReRegistrationEventTest {

    @Test
    void parsesReRegistrationFromString() throws IllFormedPatientDetailsException {
        var nhsNumber = "9876543210";
        var nemsMessageId = "123";

        var message = new JSONObject()
                .put("nhsNumber", nhsNumber)
                .put("newlyRegisteredOdsCode", "ABC12")
                .put("nemsMessageId", nemsMessageId)
                .put("lastUpdated", "some date")
                .toString();

        var expectedReRegistrationEvent = new ReRegistrationEvent(nhsNumber, nemsMessageId);

        var reRegistrationEvent = ReRegistrationEvent.parse(message);

        assertEquals(expectedReRegistrationEvent, reRegistrationEvent);
    }

}