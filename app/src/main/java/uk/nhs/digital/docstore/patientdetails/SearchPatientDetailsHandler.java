package uk.nhs.digital.docstore.patientdetails;

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
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.patientdetails.auth.AuthService;
import uk.nhs.digital.docstore.publishers.AuditPublisher;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

import java.util.Map;

@SuppressWarnings("unused")
public class SearchPatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchPatientDetailsHandler.class);

    private final ApiConfig apiConfig;
    private final PatientSearchConfig patientSearchConfig;
    private final AuditPublisher sensitiveIndex;
    private final AuthService authService = new AuthService();
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    public SearchPatientDetailsHandler() {
        this(new ApiConfig(), new PatientSearchConfig(), new SplunkPublisher());
    }

    public SearchPatientDetailsHandler(ApiConfig apiConfig, PatientSearchConfig patientSearchConfig, AuditPublisher sensitiveIndex) {
        this.apiConfig = apiConfig;
        this.patientSearchConfig = patientSearchConfig;
        this.sensitiveIndex = sensitiveIndex;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        LOGGER.debug("API Gateway event received - processing starts");
        var searchParameters = queryParametersFrom(requestEvent);

        try {
            var parameterForm = new NHSNumberSearchParameterForm(searchParameters);
            var nhsNumber = parameterForm.getNhsNumber();
            var pdsFhirClient = patientSearchConfig.pdsFhirIsStubbed()
                    ? new FakePdsFhirService(sensitiveIndex)
                    : new RealPdsFhirService(patientSearchConfig, sensitiveIndex, authService);
            var patientDetails = pdsFhirClient.fetchPatientDetails(nhsNumber);

            LOGGER.debug("Generating response body");
            var json = convertToJson(PatientDetails.fromFhirPatient(patientDetails));
            var body = getBody(json);

            LOGGER.debug("Processing finished - about to return the response");
            return apiConfig.getApiGatewayResponse(200, body, "GET", null);
        } catch (PatientNotFoundException e) {
            return apiConfig.getApiGatewayResponse(404, getBodyWithError(e), "GET", null);
        } catch (Exception exception) {
            return errorResponseGenerator.errorResponse(exception);
        }
    }

    private String getBodyWithError(Exception e) {
        return "{\n" +
                "   \"errorMessage\": \"" + e.getMessage() + "\"\n" +
                "}";
    }

    private String getBody(String patientDetails) {
        return "{\n" +
                "   \"result\": {\n" +
                "       \"patientDetails\": " + patientDetails +
                "   }\n" +
                "}";
    }

    private String convertToJson(PatientDetails patientDetails) throws JsonProcessingException {
        var objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return objectWriter.writeValueAsString(patientDetails);
    }

    private static Map<String, String> queryParametersFrom(APIGatewayProxyRequestEvent requestEvent) {
        return requestEvent.getQueryStringParameters() == null
                ? Map.of()
                : requestEvent.getQueryStringParameters();
    }
}
