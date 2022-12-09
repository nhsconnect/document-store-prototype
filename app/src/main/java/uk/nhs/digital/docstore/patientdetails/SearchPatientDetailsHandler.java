package uk.nhs.digital.docstore.patientdetails;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.exceptions.PdsException;

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

            logger.debug("Generating response body");
            var json = convertToJson(patientDetails);
            var body = getBody(json);

            logger.debug("Processing finished - about to return the response");
            return apiConfig.getApiGatewayResponse(200, body, "GET", null);
        }
        catch (PdsException e) {
            return apiConfig.getApiGatewayResponse(200, getBodyWithError(e), "GET", null);
        }
        catch (Exception e) {
            return errorResponseGenerator.errorResponse(e, fhirContext.newJsonParser());
        }
    }

    private String getBodyWithError(Exception e) {
        return "{\n" +
                "   \"error\": \""+ e.getMessage() +"\"\n" +
                "}";
    }

    private String getBody(String patientDetails) {
       return "{\n" +
               "   \"result\": {\n" +
               "       \"patientDetails\": "+ patientDetails +
               "   }\n" +
               "}";
    }
    private String convertToJson(PatientDetails patientDetails) throws JsonProcessingException {
        var ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(patientDetails);
    }

    private static Map<String, String> queryParametersFrom(APIGatewayProxyRequestEvent requestEvent) {
        return requestEvent.getQueryStringParameters() == null
                ? Map.of()
                : requestEvent.getQueryStringParameters();
    }
}
