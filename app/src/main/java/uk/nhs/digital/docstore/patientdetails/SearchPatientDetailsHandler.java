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
import uk.nhs.digital.docstore.auditmessages.PatientSearchAuditMessage;
import uk.nhs.digital.docstore.auditmessages.PdsResponseStatusType;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.publishers.AuditPublisher;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

import java.time.Instant;
import java.util.Map;

public class SearchPatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(SearchPatientDetailsHandler.class);
    private final ApiConfig apiConfig;

    private final PatientSearchConfig patientSearchConfig;
    private final AuditPublisher sensitiveIndex;
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

        logger.debug("API Gateway event received - processing starts");
        var searchParameters = queryParametersFrom(requestEvent);

        try {
            var parameterForm = new NHSNumberSearchParameterForm(searchParameters);
            var nhsNumber = parameterForm.getNhsNumber();

            var pdsFhirClient = patientSearchConfig.pdsFhirIsStubbed()
                    ? new FakePdsFhirClient()
                    : new RealPdsFhirClient(patientSearchConfig);
            var patientDetails = pdsFhirClient.fetchPatientDetails(nhsNumber);

            logger.debug("Generating response body");
            var json = convertToJson(PatientDetails.fromFhirPatient(patientDetails));
            var body = getBody(json);

            sensitiveIndex.publish(getAuditMessage(nhsNumber));
            logger.debug("Processing finished - about to return the response");
            return apiConfig.getApiGatewayResponse(200, body, "GET", null);
        }
        catch (PatientNotFoundException e) {
            return apiConfig.getApiGatewayResponse(404, getBodyWithError(e), "GET", null);
        }
        catch (Exception e) {
            return errorResponseGenerator.errorResponse(e);
        }
    }

    private String getAuditMessage(String nhsNumber) throws JsonProcessingException {
        return new PatientSearchAuditMessage(nhsNumber, PdsResponseStatusType.SUCCESS, Instant.now().toString()).toJsonString();
    }

    private String getBodyWithError(Exception e) {
        return "{\n" +
                "   \"errorMessage\": \""+ e.getMessage() +"\"\n" +
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
