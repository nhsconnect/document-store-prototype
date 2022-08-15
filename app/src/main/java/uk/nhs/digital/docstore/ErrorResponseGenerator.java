package uk.nhs.digital.docstore;

import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.create.InvalidCodingCodeException;
import uk.nhs.digital.docstore.create.MissingRequiredValueException;
import uk.nhs.digital.docstore.search.InvalidSubjectIdentifierException;
import uk.nhs.digital.docstore.search.MissingSearchParametersException;
import uk.nhs.digital.docstore.search.UnrecognisedSubjectIdentifierSystemException;

import java.util.Map;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.REQUIRED;

public class ErrorResponseGenerator {
    private static final Logger logger
            = LoggerFactory.getLogger(ErrorResponseGenerator.class);

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
        } else if (e instanceof UnrecognisedCodingSystemException) {
            statusCode = 400;
            issueType = CODEINVALID;
            errorCode = "INVALID_CODE_SYSTEM";
            errorDisplay = "Invalid code system";
        } else if (e instanceof MissingRequiredValueException) {
            statusCode = 400;
            issueType = REQUIRED;
            errorCode = "INVALID_VALUE";
            errorDisplay = "Missing required field";
        } else if (e instanceof InvalidCodingCodeException) {
            statusCode = 400;
            issueType = CODEINVALID;
            errorCode = "INVALID_CODE_VALUE";
            errorDisplay = "Invalid coding code value";
        }

        logger.error(e.getMessage(), e);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/fhir+json",
                        "Access-Control-Allow-Origin", System.getenv("AMPLIFY_BASE_URL"),
                        "Access-Control-Allow-Methods", "GET, OPTIONS, POST"))
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
