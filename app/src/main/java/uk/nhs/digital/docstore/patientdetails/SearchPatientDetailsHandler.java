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


public class SearchPatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {
    private static final Logger logger = LoggerFactory.getLogger(SearchPatientDetailsHandler.class);

    private final FhirContext fhirContext;
    private final ApiConfig apiConfig;
    private final PdsAdaptorClient pdsAdaptorClient;

    public SearchPatientDetailsHandler()
    {
        this(new ApiConfig(), new PdsAdaptorClient());
    }

    public SearchPatientDetailsHandler(ApiConfig apiConfig, PdsAdaptorClient pdsAdaptorClient)
    {
        this(FhirContext.forR4(), apiConfig, pdsAdaptorClient);
    }

    public SearchPatientDetailsHandler(FhirContext fhirContext,
                                       ApiConfig apiConfig,
                                       PdsAdaptorClient pdsAdaptorClient)
    {
        this.fhirContext = fhirContext;
        this.apiConfig = apiConfig;
        this.pdsAdaptorClient = pdsAdaptorClient;
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

        try {
            var patientDetails = pdsAdaptorClient.fetchPatientDetails(nhsNumber);
            if (patientDetails == null) {
                return emptyBundleResponse();
            }
            logger.debug("Generating response");

            BundleMapper bundleMapper = new BundleMapper();
            Bundle bundle = bundleMapper.toBundle(List.of(patientDetails));
            logger.debug("Processing finished - about to return the response");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of(
                            "Content-Type", "application/fhir+json",
                            "Access-Control-Allow-Origin", apiConfig.getAmplifyBaseUrl(),
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
