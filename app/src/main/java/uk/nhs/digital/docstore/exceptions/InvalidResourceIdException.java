package uk.nhs.digital.docstore.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;

public class InvalidResourceIdException extends RuntimeException implements OperationOutcomeIssuable {
    public InvalidResourceIdException(String nhsNumber) {
        super(String.format("invalid nhs number: '%s'", nhsNumber));
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent toOperationOutcomeIssue() {
        return new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(ERROR)
                .setCode(CODEINVALID)
                .setDetails(new CodeableConcept()
                        .addCoding(new Coding()
                                .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                .setCode("INVALID_RESOURCE_ID")
                                .setDisplay("Resource Id is invalid")));
    }
}
