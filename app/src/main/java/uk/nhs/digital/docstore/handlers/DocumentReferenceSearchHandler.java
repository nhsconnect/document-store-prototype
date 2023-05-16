package uk.nhs.digital.docstore.handlers;

import static ca.uhn.fhir.context.PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING;

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
import uk.nhs.digital.docstore.BundleMapper;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.services.DocumentMetadataSearchService;

@SuppressWarnings("unused")
public class DocumentReferenceSearchHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DocumentReferenceSearchHandler.class);
    private static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");

    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
    private final BundleMapper bundleMapper = new BundleMapper();

    private final DocumentMetadataSearchService searchService;
    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;

    public DocumentReferenceSearchHandler() {
        this(
                new DocumentMetadataSearchService(
                        new DocumentMetadataStore(), new DocumentMetadataSerialiser()));
    }

    public DocumentReferenceSearchHandler(DocumentMetadataSearchService searchService) {
        this.apiConfig = new ApiConfig();
        this.fhirContext = FhirContext.forR4();
        this.fhirContext.setPerformanceOptions(DEFERRED_MODEL_SCANNING);
        this.searchService = searchService;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);
        LOGGER.debug("API Gateway event received - processing starts");

        var jsonParser = fhirContext.newJsonParser();

        LOGGER.debug("Querying DynamoDB");
        Bundle bundle;

        try {
            var nhsNumberSearchParameterForm =
                    new NHSNumberSearchParameterForm(requestEvent.getQueryStringParameters());
            var nhsNumber = nhsNumberSearchParameterForm.getNhsNumber();
            var documents = searchService.findMetadataByNhsNumber(nhsNumber);
            LOGGER.debug("Generating response contents");
            bundle = bundleMapper.toBundle(documents);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e);
        }

        LOGGER.debug("Processing finished - about to return the response");
        var body = jsonParser.encodeResourceToString(bundle);
        return apiConfig.getApiGatewayResponse(200, body, "GET", null);
    }
}
