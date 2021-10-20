package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

import java.util.Map;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;

public class RetrieveDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DocumentReferenceStore store = new DocumentReferenceStore();
    private final FhirContext fhirContext;

    public RetrieveDocumentReferenceHandler() {
        this.fhirContext = FhirContext.forR4();
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        var jsonParser = fhirContext.newJsonParser();

        var resource = store.getById(event.getPathParameters().get("id"));

        if (resource == null) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(404)
                    .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                    .withBody(jsonParser.encodeResourceToString(new OperationOutcome()
                            .addIssue(new OperationOutcome.OperationOutcomeIssueComponent()
                                    .setSeverity(ERROR)
                                    .setCode(NOTFOUND)
                                    .setDetails(new CodeableConcept()
                                            .addCoding(new Coding()
                                                    .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                                    .setCode("NO_RECORD_FOUND")
                                                    .setDisplay("No record found"))))));
        }

        var resourceAsJson = jsonParser.encodeResourceToString(resource);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                .withBody(resourceAsJson);
    }
}
