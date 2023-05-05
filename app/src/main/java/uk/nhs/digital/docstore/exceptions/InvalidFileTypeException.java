package uk.nhs.digital.docstore.exceptions;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.REQUIRED;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

public class InvalidFileTypeException extends RuntimeException implements OperationOutcomeIssuable {
    private final String path;

    public InvalidFileTypeException(String path, String fileExt) {
        super(String.format("File type '%s' is not accepted", fileExt));
        this.path = path;
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent toOperationOutcomeIssue() {
        return new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(ERROR)
                .setCode(REQUIRED)
                .addExpression(this.path)
                .setDetails(
                        new CodeableConcept()
                                .addCoding(
                                        new Coding()
                                                .setSystem(
                                                        "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                                                .setCode("INVALID_VALUE")
                                                .setDisplay("Invalid file type")));
    }
}
