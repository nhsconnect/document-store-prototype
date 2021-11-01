package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.Bundle;

import java.util.List;
import java.util.Map;

import static ca.uhn.fhir.context.PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING;

@SuppressWarnings("unused")
public class DocumentReferenceSearchHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final BundleMapper bundleMapper = new BundleMapper();
    private final DocumentReferenceSearchService searchService;
    private final FhirContext fhirContext;

    public DocumentReferenceSearchHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(DEFERRED_MODEL_SCANNING);

        DocumentMetadataStore metadataStore = new DocumentMetadataStore();
        DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
        this.searchService = new DocumentReferenceSearchService(metadataStore, documentStore);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        var jsonParser = fhirContext.newJsonParser();

        List<Document> documents = searchService.findByParameters(requestEvent.getQueryStringParameters());
        Bundle bundle = bundleMapper.toBundle(documents);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                .withBody(jsonParser.encodeResourceToString(bundle));
    }
}
