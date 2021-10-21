package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;

import java.util.Map;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.NOTFOUND;

public class RetrieveDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DocumentReferenceStore store = new DocumentReferenceStore();
    private final FhirContext fhirContext;

    public RetrieveDocumentReferenceHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        System.out.println("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();

        var metadata = store.getById(event.getPathParameters().get("id"));

        if (metadata == null) {
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

        System.out.println("Retrieved the request object - about to transform it into JSON");

        var resource = new DocumentReference()
                .setSubject(new Reference()
                .setIdentifier(new Identifier()
                        .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                        .setValue(metadata.getNhsNumber())))
                .setId(metadata.getId());
        var resourceAsJson = jsonParser.encodeResourceToString(resource);

        System.out.println("Processing finished - about to return the response");
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                .withBody(resourceAsJson);
    }
}
