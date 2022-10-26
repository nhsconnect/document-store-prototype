package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import ca.uhn.fhir.parser.IParser;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.exceptions.*;

import java.util.Map;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.EXCEPTION;

public class ErrorResponseGenerator {
    private static final Logger logger
            = LoggerFactory.getLogger(ErrorResponseGenerator.class);

    private final ApiConfig apiConfig;

    public ErrorResponseGenerator() {
        this(new ApiConfig());
    }

    public ErrorResponseGenerator(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public APIGatewayProxyResponseEvent errorResponse(Exception e, IParser jsonParser) {
        int statusCode;
        OperationOutcome.OperationOutcomeIssueComponent operationOutcomeIssueComponent;

        if (e instanceof OperationOutcomeIssuable) {
            statusCode = 400;
            operationOutcomeIssueComponent = ((OperationOutcomeIssuable) e).toOperationOutcomeIssue();
        }  else {
            statusCode = 500;
            operationOutcomeIssueComponent = new OperationOutcome.OperationOutcomeIssueComponent()
                    .setSeverity(ERROR)
                    .setCode(EXCEPTION)
                    .setDetails(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                    .setCode("INTERNAL_SERVER_ERROR")
                                    .setDisplay("Internal server error")));
        }

        logger.error(e.getMessage(), e);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/fhir+json",
                        "Access-Control-Allow-Origin", apiConfig.getAmplifyBaseUrl(),
                        "Access-Control-Allow-Methods", "GET, OPTIONS, POST"))
                .withBody(jsonParser.encodeResourceToString(new OperationOutcome()
                        .addIssue(operationOutcomeIssueComponent)));
    }
}
