package uk.nhs.digital.docstore.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.REQUIRED;

import java.util.List;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;

public class MissingRequiredValueExceptionTest {

  @Test
  public void returnsOperationOutcomeIssueComponentWithExpectedAttributes() {
    MissingRequiredValueException exception = new MissingRequiredValueException("path", "field");
    OperationOutcome.OperationOutcomeIssueComponent operationOutcomeIssueComponent =
        exception.toOperationOutcomeIssue();
    CodeableConcept details = operationOutcomeIssueComponent.getDetails();
    List<Coding> coding = details.getCoding();
    assertThat(operationOutcomeIssueComponent.getCode()).isEqualTo(REQUIRED);
    assertThat(operationOutcomeIssueComponent.getSeverity()).isEqualTo(ERROR);
    assertThat(coding.get(0).getSystem())
        .isEqualTo("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1");
    assertThat(coding.get(0).getCode()).isEqualTo("INVALID_VALUE");
    assertThat(coding.get(0).getDisplay()).isEqualTo("Missing required field");
    assertThat(operationOutcomeIssueComponent.getExpression().get(0).toString()).isEqualTo("path");
  }
}
