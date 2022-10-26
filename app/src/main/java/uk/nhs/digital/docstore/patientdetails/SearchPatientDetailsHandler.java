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

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static java.net.http.HttpClient.newHttpClient;
import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.nhs.digital.docstore.config.ApiConfig.getAmplifyBaseUrl;


public class SearchPatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {
    private static final Logger logger
            = LoggerFactory.getLogger(SearchPatientDetailsHandler.class);

    private final FhirContext fhirContext;
    private final PatientDetailsMapper patientDetailsMapper;
    private final PatientSearchConfig patientSearchConfig;

    public SearchPatientDetailsHandler()
    {
        this(new PatientSearchConfig());
    }

    public SearchPatientDetailsHandler(PatientSearchConfig patientSearchConfig)
    {
        this(FhirContext.forR4(), new PatientDetailsMapper(), patientSearchConfig);
    }

    public SearchPatientDetailsHandler(FhirContext fhirContext, PatientDetailsMapper patientDetailsMapper, PatientSearchConfig patientSearchConfig)
    {
        this.fhirContext = fhirContext;
        this.patientDetailsMapper = patientDetailsMapper;
        this.patientSearchConfig = patientSearchConfig;
    }


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        Tracer.setMDCContext(context);

        logger.debug("API Gateway event received - processing starts");
        var jsonParser = fhirContext.newJsonParser();
        String nhsNumber = "";

        Map<String, String> searchParameters = (requestEvent.getQueryStringParameters() == null
                ? Map.of()
                : requestEvent.getQueryStringParameters());

        try {
            NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(searchParameters);
            nhsNumber = nhsNumberSearchParameterForm.getNhsNumber();
        } catch (Exception e) {
            ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
            return errorResponseGenerator.errorResponse(e, jsonParser);
        }

        logger.debug("Confirming NHS number with PDS adaptor");
        var confirmNHSNumberRequest = HttpRequest.newBuilder(
                URI.create(patientSearchConfig.pdsAdaptorRootUri() +
                        "patient-trace-information/" + nhsNumber))
                .GET()
                .build();

        try {
            var confirmNHSNumberResponse = newHttpClient().send(confirmNHSNumberRequest, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (confirmNHSNumberResponse.statusCode() == 404) {
                return emptyBundleResponse();
            }
            logger.debug("Generating response");

            var patientDetails = patientDetailsMapper.fromPatientDetailsResponseBody(confirmNHSNumberResponse.body());

            logger.debug("Generating response");

            BundleMapper bundleMapper = new BundleMapper();
            Bundle bundle = bundleMapper.toBundle(List.of(patientDetails));
            logger.debug("Processing finished - about to return the response");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of(
                            "Content-Type", "application/fhir+json",
                            "Access-Control-Allow-Origin", getAmplifyBaseUrl(),
                            "Access-Control-Allow-Methods", "GET"))
                    .withBody(jsonParser.encodeResourceToString(bundle));
        } catch (Exception e) {
            ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();
            return errorResponseGenerator.errorResponse(e, jsonParser);
        }
    }

    private static APIGatewayProxyResponseEvent emptyBundleResponse() {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json"))
                .withBody("{\n" +
                        "  \"resourceType\": \"Bundle\",\n" +
                        "  \"type\": \"searchset\",\n" +
                        "  \"total\": 0\n" +
                        "}");
    }
}
