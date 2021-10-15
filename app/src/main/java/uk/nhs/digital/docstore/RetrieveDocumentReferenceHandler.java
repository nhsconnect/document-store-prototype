package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;


public class RetrieveDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final FhirContext fhirContext;

    public RetrieveDocumentReferenceHandler() {
        this.fhirContext = FhirContext.forR4();
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        var jsonParser = fhirContext.newJsonParser();

        var resource = new DocumentReference()
                .addIdentifier(new Identifier()
                        .setSystem("https://fhir.nhs.uk/Id/cross-care-setting-identifier")
                        .setValue("bb237762-dde2-11e9-9d36-2a2ae2dbcce4"))
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                .setValue("12345")))
                .setStatus(Enumerations.DocumentReferenceStatus.CURRENT)
                .setType(new CodeableConcept().addCoding(new Coding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("824331000000106")
                        .setDisplay("Inpatient final discharge letter")))
                .setMeta(new Meta().addProfile("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1"))
                .setId("1234");
        // no method to set indexed value

        var resourceAsJson = jsonParser.encodeResourceToString(resource);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(resourceAsJson);
    }
}
