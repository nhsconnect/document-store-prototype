package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class RetrieveDocumentReferenceHandlerTest {
    @Test
    void returnsErrorWhenNoMatchingDocumentIsFound() {
        var handler = new RetrieveDocumentReferenceHandler();
        var requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setPathParameters(Map.of("id", "does-not-exist"));

        var response = handler.handleRequest(requestEvent, null);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThatJson(response.getBody()).isEqualTo("{\n" +
                "  \"resourceType\": \"OperationOutcome\",\n" +
                "  \"issue\": [{\n" +
                "    \"severity\": \"error\",\n" +
                "    \"code\": \"not-found\",\n" +
                "    \"details\": {\n" +
                "      \"coding\": [{\n" +
                "        \"system\": \"https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1\",\n" +
                "        \"code\": \"NO_RECORD_FOUND\",\n" +
                "        \"display\": \"No record found\"\n" +
                "      }]\n" +
                "    }\n" +
                "  }]\n" +
                "}");
    }
}