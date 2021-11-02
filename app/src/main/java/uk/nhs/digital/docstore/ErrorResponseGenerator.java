package uk.nhs.digital.docstore;

import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

import java.util.Map;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;

public class ErrorResponseGenerator {

    public APIGatewayProxyResponseEvent errorResponse(Exception e, IParser jsonParser) {
        int statusCode = 500;
        OperationOutcome.IssueType issueType = EXCEPTION;
        String errorCode = "INTERNAL_SERVER_ERROR";
        String errorDisplay = "Internal server error";

        if (e instanceof UnrecognisedSubjectIdentifierSystemException) {
            statusCode = 400;
            issueType = CODEINVALID;
            errorCode = "INVALID_IDENTIFIER_SYSTEM";
            errorDisplay = "Invalid identifier system";
        } else if (e instanceof InvalidSubjectIdentifierException) {
            statusCode = 400;
            issueType = CODEINVALID;
            errorCode = "INVALID_IDENTIFIER_VALUE";
            errorDisplay = "Invalid identifier value";
        } else if (e instanceof MissingSearchParametersException) {
            statusCode = 400;
            issueType = CODEINVALID;
            errorCode = "INVALID_PARAMETER";
            errorDisplay = "Invalid parameter";
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/fhir+json"))
                .withBody(jsonParser.encodeResourceToString(new OperationOutcome()
                        .addIssue(new OperationOutcome.OperationOutcomeIssueComponent()
                                .setSeverity(ERROR)
                                .setCode(issueType)
                                .setDetails(new CodeableConcept()
                                        .addCoding(new Coding()
                                                .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                                .setCode(errorCode)
                                                .setDisplay(errorDisplay))))));
    }
}
