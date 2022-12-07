package uk.nhs.digital.docstore.patientdetails;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.Bundle;
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
    private final PdsFhirClient pdsFhirClient;
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    public SearchPatientDetailsHandler() {
        this(new ApiConfig(), new PdsFhirClient());
    }

    public SearchPatientDetailsHandler(ApiConfig apiConfig, PdsFhirClient pdsFhirClient) {
        this(FhirContext.forR4(), apiConfig, pdsFhirClient);
    }

    public SearchPatientDetailsHandler(FhirContext fhirContext, ApiConfig apiConfig, PdsFhirClient pdsFhirClient) {
        this.fhirContext = fhirContext;
        this.apiConfig = apiConfig;
        this.pdsFhirClient = pdsFhirClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");
        var searchParameters = queryParametersFrom(requestEvent);

        try {
            var parameterForm = new NHSNumberSearchParameterForm(searchParameters);

            var patientDetails = pdsFhirClient.fetchPatientDetails(parameterForm.getNhsNumber());
            if (patientDetails == null) {
                return emptyBundleResponse();
            }

            logger.debug("Generating response body");
            var body = toJson(fhirBundleOf(patientDetails));

            logger.debug("Processing finished - about to return the response");
            return apiConfig.getApiGatewayResponse(200, body, "GET", null);
        }
        catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, fhirContext.newJsonParser());
        }
    }

    private APIGatewayProxyResponseEvent emptyBundleResponse() {
        var body = "{\n" +
                "  \"resourceType\": \"Bundle\",\n" +
                "  \"type\": \"searchset\",\n" +
                "  \"total\": 0\n" +
                "}";
        return apiConfig.getApiGatewayResponse(200, body, "GET", null);
    }

    private static Map<String, String> queryParametersFrom(APIGatewayProxyRequestEvent requestEvent) {
        return requestEvent.getQueryStringParameters() == null
                ? Map.of()
                : requestEvent.getQueryStringParameters();
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
