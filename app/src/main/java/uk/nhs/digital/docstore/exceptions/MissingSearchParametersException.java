package uk.nhs.digital.docstore.exceptions;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

public class MissingSearchParametersException extends RuntimeException
        implements OperationOutcomeIssuable {
    public MissingSearchParametersException(String expectedParameter) {
        super(String.format("Expected '%s' but was missing", expectedParameter));
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent toOperationOutcomeIssue() {
        return new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(ERROR)
                .setCode(CODEINVALID)
                .setDetails(
                        new CodeableConcept()
                                .addCoding(
                                        new Coding()
                                                .setSystem(
                                                        "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                                .setCode("INVALID_PARAMETER")
                                                .setDisplay("Invalid parameter")));
    }
}
