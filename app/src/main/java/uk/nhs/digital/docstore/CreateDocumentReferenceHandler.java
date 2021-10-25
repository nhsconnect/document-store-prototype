package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

import java.util.Map;

@SuppressWarnings("unused")
public class CreateDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final FhirContext fhirContext;

    public CreateDocumentReferenceHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        System.out.println("API Gateway event received - processing starts");

        var jsonParser = fhirContext.newJsonParser();

        var inputDocumentReference = jsonParser.parseResource(DocumentReference.class, input.getBody());

        var documentMetadata = DocumentMetadata.from(inputDocumentReference);

        var savedDocumentMetadata = metadataStore.save(documentMetadata);

        String hostHeader = input.getHeaders().get("Host");

        var resource = new DocumentReference()
                .setSubject(new Reference()
                        .setIdentifier(new Identifier()
                                .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                .setValue(savedDocumentMetadata.getNhsNumber())))
                .addContent(new DocumentReference.DocumentReferenceContentComponent()
                        .setAttachment(new Attachment()
                                .setUrl("ignore-this")
                                .setContentType(savedDocumentMetadata.getContentType())))
                .setId(savedDocumentMetadata.getId());
        var resourceAsJson = jsonParser.encodeResourceToString(resource);
        String locationHeader = "https://" + hostHeader + "/DocumentReference/" + savedDocumentMetadata.getId();

        System.out.println("Processing finished - about to return the response");
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withHeaders(Map.of("Content-Type", "application/fhir+json", "Location", locationHeader))
                .withBody(resourceAsJson);
    }
}
