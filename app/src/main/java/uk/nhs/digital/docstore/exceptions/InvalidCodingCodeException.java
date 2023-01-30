package uk.nhs.digital.docstore.exceptions;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

public class InvalidCodingCodeException extends RuntimeException
        implements OperationOutcomeIssuable {
    private final String path;

    public InvalidCodingCodeException(String path, String code) {
        super(String.format("Invalid coding code (%s) at path: '%s'", code, path));
        this.path = path;
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent toOperationOutcomeIssue() {
        return new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(ERROR)
                .setCode(CODEINVALID)
                .addExpression(this.path)
                .setDetails(
                        new CodeableConcept()
                                .addCoding(
                                        new Coding()
                                                .setSystem(
                                                        "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                                .setCode("INVALID_CODE_VALUE")
                                                .setDisplay("Invalid coding code value")));
    }
}
