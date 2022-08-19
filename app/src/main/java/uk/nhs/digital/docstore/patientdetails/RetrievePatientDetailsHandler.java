package uk.nhs.digital.docstore.patientdetails;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;
import uk.nhs.digital.docstore.ErrorResponseGenerator;
import uk.nhs.digital.docstore.exceptions.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.exceptions.MissingSearchParametersException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedSubjectIdentifierSystemException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RetrievePatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {
    private static final Pattern SUBJECT_IDENTIFIER_PATTERN = Pattern.compile("^(?<systempart>(?<system>.*?)(?<!\\\\)\\|)?(?<identifier>\\d{10})$");
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private final FhirContext fhirContext;

    public RetrievePatientDetailsHandler() {
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
            String subject = Optional.ofNullable(searchParameters.get("subject:identifier"))
                    .or(() -> Optional.ofNullable(searchParameters.get("subject.identifier")))
                    .orElseThrow(() -> new MissingSearchParametersException("subject:identifier"));
            nhsNumber = validSubject(subject);
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
                        "Content-Type", "application/fhir+json"))
                .withBody(jsonParser.encodeResourceToString(bundle));
    }

    private String validSubject(String subject) {
        Matcher matcher = SUBJECT_IDENTIFIER_PATTERN.matcher(subject);
        if (!matcher.matches() || matcher.group("identifier").isBlank()) {
            throw new InvalidSubjectIdentifierException(subject);
        }
        if (matcher.group("systempart") != null && !NHS_NUMBER_SYSTEM_ID.equals(matcher.group("system"))) {
            throw new UnrecognisedSubjectIdentifierSystemException(matcher.group("system"));
        }

        return matcher.group("identifier");
    }
}
