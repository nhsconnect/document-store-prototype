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
import uk.nhs.digital.docstore.config.Tracer;

import java.util.List;
import java.util.Map;


public class SearchPatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {
    private static final Logger logger
            = LoggerFactory.getLogger(SearchPatientDetailsHandler.class);
    private final FhirContext fhirContext;

    public SearchPatientDetailsHandler() {
        this.fhirContext = FhirContext.forR4();
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

        /* Currently, we are providing fake responses based on the PDS API sandbox environment test scenarios. See here for more information:
        https://digital.nhs.uk/developer/api-catalogue/personal-demographics-service-fhir#api-Default-get-patient*/
        logger.debug("Generating response body");
        List<PatientDetails> patientDetailsList;
        if (nhsNumber.equals("9000000009")) {
            PatientDetails patientDetails = new PatientDetails(List.of("Jane"), "Doe", "1998-07-11", "LS1 6AE", "9000000009");
            patientDetailsList = List.of(patientDetails);
        } else if (nhsNumber.equals("9111231130")) {
            patientDetailsList = List.of();
        } else throw new RuntimeException("Unexpected NHS number");

        BundleMapper bundleMapper = new BundleMapper();
        Bundle bundle = bundleMapper.toBundle(patientDetailsList);
        logger.debug("Processing finished - about to return the response");
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json",
                        "Access-Control-Allow-Origin", System.getenv("AMPLIFY_BASE_URL"),
                        "Access-Control-Allow-Methods", "GET"))
                .withBody(jsonParser.encodeResourceToString(bundle));
    }
}
