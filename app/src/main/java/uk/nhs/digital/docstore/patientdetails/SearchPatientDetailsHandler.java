package uk.nhs.digital.docstore.patientdetails;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;

import java.util.List;
import java.util.Map;


public class SearchPatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {
    private final FhirContext fhirContext;

    public SearchPatientDetailsHandler() {
        this.fhirContext = FhirContext.forR4();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
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

        List<PatientDetails> patientDetailsList;
        if (nhsNumber.equals("9000000009")) {
            PatientDetails patientDetails = new PatientDetails(List.of("Jane"), "Doe", "1998-07-11", "LS1 6AE", "9000000009");
            patientDetailsList = List.of(patientDetails);
        } else if (nhsNumber.equals("9111231130")) {
            patientDetailsList = List.of();
        } else throw new RuntimeException("Unexpected NHS number");

        BundleMapper bundleMapper = new BundleMapper();
        Bundle bundle = bundleMapper.toBundle(patientDetailsList);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json",
                        "Access-Control-Allow-Origin", System.getenv("AMPLIFY_BASE_URL"),
                        "Access-Control-Allow-Methods", "GET"))
                .withBody(jsonParser.encodeResourceToString(bundle));
    }
}
