package uk.nhs.digital.docstore.presignedurl;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import uk.nhs.digital.docstore.Document;

import uk.nhs.digital.docstore.DocumentMetadataStore;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.search.BundleMapper;


import java.util.List;
import java.util.Map;

import static ca.uhn.fhir.context.PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING;

@SuppressWarnings("unused")
public class PresignedUrlHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(PresignedUrlHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
    private final BundleMapper bundleMapper = new BundleMapper();
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final PresignedUrlService presignedUrlService;
    private final FhirContext fhirContext;

    public PresignedUrlHandler() {
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(DEFERRED_MODEL_SCANNING);

        DocumentMetadataStore metadataStore = new DocumentMetadataStore();
        DocumentStore documentStore = new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME"));
        this.presignedUrlService = new PresignedUrlService(metadataStore, documentStore);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();

        logger.debug("Querying DynamoDB");
        List<Document> documents;
        Bundle bundle;
        try {
            Map<String, String> searchParameters = (requestEvent.getQueryStringParameters() == null
                    ? Map.of()
                    : requestEvent.getQueryStringParameters());
            documents = List.of(presignedUrlService.findByParameters(searchParameters));
            logger.debug("Generating response contents");
            bundle = bundleMapper.toBundle(documents);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, jsonParser);
        }
        var resourceAsJson = jsonParser.encodeResourceToString(bundle);
        logger.debug("Processing finished - about to return the response");
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json",
                        "Access-Control-Allow-Origin", System.getenv("AMPLIFY_BASE_URL"),
                        "Access-Control-Allow-Methods", "GET"
                ))
                .withBody(resourceAsJson);
    }
}
