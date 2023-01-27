package uk.nhs.digital.docstore.exceptions;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.REQUIRED;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;

public class MissingRequiredValueException extends RuntimeException
    implements OperationOutcomeIssuable {
  private final String path;

  public MissingRequiredValueException(String path, String field) {
    super(String.format("Missing required field: '%s' at path %s", field, path));
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
                        .setSystem("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1")
                        .setCode("INVALID_VALUE")
                        .setDisplay("Missing required field")));
  }
}
