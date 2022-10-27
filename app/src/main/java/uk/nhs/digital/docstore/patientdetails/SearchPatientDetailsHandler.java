package uk.nhs.digital.docstore.patientdetails;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;

import java.util.List;
import java.util.Map;

public class SearchPatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(SearchPatientDetailsHandler.class);

    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;
    private final PdsAdaptorClient pdsAdaptorClient;
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    public SearchPatientDetailsHandler() {
        this(new ApiConfig(), new PdsAdaptorClient());
    }

    public SearchPatientDetailsHandler(ApiConfig apiConfig, PdsAdaptorClient pdsAdaptorClient) {
        this(FhirContext.forR4(), apiConfig, pdsAdaptorClient);
    }

    public SearchPatientDetailsHandler(FhirContext fhirContext, ApiConfig apiConfig, PdsAdaptorClient pdsAdaptorClient) {
        this.fhirContext = fhirContext;
        this.apiConfig = apiConfig;
        this.pdsAdaptorClient = pdsAdaptorClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");
        var searchParameters = queryParametersFrom(requestEvent);

        try {
            var parameterForm = new NHSNumberSearchParameterForm(searchParameters);

            var patientDetails = pdsAdaptorClient.fetchPatientDetails(parameterForm.getNhsNumber());
            if (patientDetails == null) {
                return emptyBundleResponse();
            }

            logger.debug("Generating response body");
            var body = toJson(fhirBundleOf(patientDetails));

            logger.debug("Processing finished - about to return the response");
            return fhirResponse(200).withBody(body);
        }
        catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, fhirContext.newJsonParser());
        }
    }

    private APIGatewayProxyResponseEvent emptyBundleResponse() {
        return fhirResponse(200).withBody("{\n" +
                "  \"resourceType\": \"Bundle\",\n" +
                "  \"type\": \"searchset\",\n" +
                "  \"total\": 0\n" +
                "}");
    }

    private static Map<String, String> queryParametersFrom(APIGatewayProxyRequestEvent requestEvent) {
        return requestEvent.getQueryStringParameters() == null
                ? Map.of()
                : requestEvent.getQueryStringParameters();
    }

    private APIGatewayProxyResponseEvent fhirResponse(int statusCode) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json",
                        "Access-Control-Allow-Origin", apiConfig.getAmplifyBaseUrl(),
                        "Access-Control-Allow-Methods", "GET"));
    }

    private static Bundle fhirBundleOf(PatientDetails patientDetails) {
        BundleMapper bundleMapper = new BundleMapper();
        return bundleMapper.toBundle(List.of(patientDetails));
    }

    private String toJson(Bundle payload) {
        var jsonParser = fhirContext.newJsonParser();
        return jsonParser.encodeResourceToString(payload);
    }
}
