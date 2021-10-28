package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.PRELIMINARY;

@SuppressWarnings("unused")
public class DocumentReferenceSearchHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final FhirContext fhirContext;

    public DocumentReferenceSearchHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        var jsonParser = fhirContext.newJsonParser();
        int total = 0;
        List<Bundle.BundleEntryComponent> searchResults = new ArrayList<>();

        if (requestEvent.getQueryStringParameters().get("subject:identifier").equals("https://fhir.nhs.uk/Id/nhs-number|12345")) {
            total = 1;
            searchResults.add(new Bundle.BundleEntryComponent()
                    .setResource(new DocumentReference()
                            .setSubject(new Reference()
                                    .setIdentifier(new Identifier()
                                            .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                            .setValue("12345")))
                            .setDocStatus(PRELIMINARY)
                            .setId("1234"))
                    .setFullUrl("http://172.17.0.2:4566/DocumentReference/1234"));
        }

        var bundle = new Bundle()
                .setTotal(total)
                .setType(Bundle.BundleType.SEARCHSET)
                .setEntry(searchResults);
        String body = jsonParser.encodeResourceToString(bundle);


        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json"))
                .withBody(body);
    }
}
