package uk.nhs.digital.docstore.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;


public class MissingSearchParametersExceptionTest {

    @Test
    public void returnsOperationOutcomeIssueComponentWithExpectedAttributes() {
       MissingSearchParametersException exception = new MissingSearchParametersException("expected-parameter");
        OperationOutcome.OperationOutcomeIssueComponent operationOutcomeIssueComponent = exception.toOperationOutcomeIssue();
        CodeableConcept details = operationOutcomeIssueComponent.getDetails();
        List<Coding> coding = details.getCoding();
        assertThat(operationOutcomeIssueComponent.getCode()).isEqualTo(CODEINVALID);
        assertThat(operationOutcomeIssueComponent.getSeverity()).isEqualTo(ERROR);
        assertThat(coding.get(0).getSystem()).isEqualTo("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1");
        assertThat(coding.get(0).getCode()).isEqualTo("INVALID_PARAMETER");
        assertThat(coding.get(0).getDisplay()).isEqualTo("Invalid parameter");
    }
}