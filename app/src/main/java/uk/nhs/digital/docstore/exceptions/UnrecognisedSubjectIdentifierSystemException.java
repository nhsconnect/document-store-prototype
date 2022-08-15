package uk.nhs.digital.docstore.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;

public class UnrecognisedSubjectIdentifierSystemException extends RuntimeException implements OperationOutcomeIssuable {
    public UnrecognisedSubjectIdentifierSystemException(String subjectIdentifierSystem) {
        super(String.format("Unrecognised system: '%s'", subjectIdentifierSystem));
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent toOperationOutcomeIssue() {
        return new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(ERROR)
                .setCode(CODEINVALID)
                .setDetails(new CodeableConcept()
                        .addCoding(new Coding()
                                .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                .setCode("INVALID_IDENTIFIER_SYSTEM")
                                .setDisplay("Invalid identifier system")));
    }
}
