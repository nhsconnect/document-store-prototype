package uk.nhs.digital.docstore.search;

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
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.utils.CommonUtils;
import uk.nhs.digital.docstore.utils.DocumentMetadataSearchService;

import static ca.uhn.fhir.context.PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public class DocumentReferenceSearchHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DocumentReferenceSearchHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final BundleMapper bundleMapper = new BundleMapper();
    private final DocumentMetadataSearchService searchService;
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;
    private final CommonUtils utils = new CommonUtils();

    public DocumentReferenceSearchHandler() {
        this(new ApiConfig());
    }

    public DocumentReferenceSearchHandler(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(DEFERRED_MODEL_SCANNING);

        DocumentMetadataStore metadataStore = new DocumentMetadataStore();
        this.searchService = new DocumentMetadataSearchService(metadataStore);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();

        logger.debug("Querying DynamoDB");
        Bundle bundle;
        try {
            var nhsNumber = utils.getNhsNumberFrom(requestEvent.getQueryStringParameters());
            var documentMetadata = searchService.findMetadataByNhsNumber(nhsNumber, requestEvent.getHeaders());

            var documents = documentMetadata.stream().map(metadata -> new Document(metadata)).collect(toList());

            logger.debug("Generating response contents");
            bundle = bundleMapper.toBundle(documents);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, jsonParser);
        }

        logger.debug("Processing finished - about to return the response");
        var body = jsonParser.encodeResourceToString(bundle);
        return apiConfig.getApiGatewayResponse(200, body, "GET", null);
    }
}
