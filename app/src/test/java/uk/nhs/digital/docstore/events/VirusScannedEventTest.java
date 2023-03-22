package uk.nhs.digital.docstore.events;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class VirusScannedEventTest {

    @Test
    void parseVirusScannedEventFromJson() throws JsonProcessingException {
        String bucketName = "some-bucket-name";
        String key = "some-key";
        String result = "Infected";

        var expectedVirusScannedEvent = new VirusScannedEvent(bucketName, key, result);

        var json = new JSONObject();
        json.put("bucketName", bucketName);
        json.put("key", key);
        json.put("result", result);
        json.put("dateScanned", "some-date");

        var actualVirusScannedEvent = VirusScannedEvent.parse(json.toString());

        assertEquals(expectedVirusScannedEvent, actualVirusScannedEvent);
    }
}
