package uk.nhs.digital.docstore.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.model.PatientDetails;
import uk.nhs.digital.docstore.patientdetails.ClientPatientDetailsDto;
import uk.nhs.digital.docstore.patientdetails.FakePdsFhirService;
import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;
import uk.nhs.digital.docstore.patientdetails.RealPdsFhirService;
import uk.nhs.digital.docstore.patientdetails.auth.AuthService;
import uk.nhs.digital.docstore.patientdetails.auth.AuthServiceHttpClient;

@SuppressWarnings("unused")
public class SearchPatientDetailsHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchPatientDetailsHandler.class);

    private final ApiConfig apiConfig;
    private final PatientSearchConfig patientSearchConfig;
    private final AuditPublisher sensitiveIndex;
    private final AuthService authService;
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    public SearchPatientDetailsHandler() {
        this(
                new ApiConfig(),
                new PatientSearchConfig(),
                new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")));
    }

    public SearchPatientDetailsHandler(
            ApiConfig apiConfig,
            PatientSearchConfig patientSearchConfig,
            AuditPublisher sensitiveIndex) {
        this.apiConfig = apiConfig;
        this.patientSearchConfig = patientSearchConfig;
        this.sensitiveIndex = sensitiveIndex;
        this.authService = new AuthService(new AuthServiceHttpClient(), patientSearchConfig);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);
        LOGGER.debug("Patient request:" + requestEvent);

        var searchParameters = queryParametersFrom(requestEvent);

        try {
            var parameterForm = new NHSNumberSearchParameterForm(searchParameters);
            var nhsNumber = parameterForm.getNhsNumber();
            var pdsFhirClient =
                    patientSearchConfig.pdsFhirIsStubbed()
                            ? new FakePdsFhirService(sensitiveIndex)
                            : new RealPdsFhirService(
                                    patientSearchConfig, sensitiveIndex, authService);
            var patientDetails = pdsFhirClient.fetchPatientDetails(nhsNumber);

            var json = convertToJson(patientDetails);
            var body = getBody(json);

            var apiGatewayResponse = apiConfig.getApiGatewayResponse(200, body, "GET", null);
            LOGGER.debug("Response: " + apiGatewayResponse);
            return apiGatewayResponse;
        } catch (PatientNotFoundException e) {
            LOGGER.debug("Patient not found - error: " + e.getMessage());
            return apiConfig.getApiGatewayResponse(404, getBodyWithError(e), "GET", null);
        } catch (InvalidResourceIdException e) {
            LOGGER.debug("Invalid NHS number - error: " + e.getMessage());
            return apiConfig.getApiGatewayResponse(400, getBodyWithError(e), "GET", null);
        } catch (Exception exception) {
            LOGGER.debug("Unexpected error: " + exception.getMessage());
            return errorResponseGenerator.errorResponse(exception);
        }
    }

    private String getBodyWithError(Exception e) {
        return "{\n" + "   \"errorMessage\": \"" + e.getMessage() + "\"\n" + "}";
    }

    private String getBody(String clientPatientDetails) {
        return "{\n"
                + "   \"result\": {\n"
                + "       \"patientDetails\": "
                + clientPatientDetails
                + "   }\n"
                + "}";
    }

    private String convertToJson(PatientDetails patientDetails) throws JsonProcessingException {
        var clientPatientDetails = ClientPatientDetailsDto.fromPatientDetails(patientDetails);
        var objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return objectWriter.writeValueAsString(clientPatientDetails);
    }

    private static Map<String, String> queryParametersFrom(
            APIGatewayProxyRequestEvent requestEvent) {
        return requestEvent.getQueryStringParameters() == null
                ? Map.of()
                : requestEvent.getQueryStringParameters();
    }
}
