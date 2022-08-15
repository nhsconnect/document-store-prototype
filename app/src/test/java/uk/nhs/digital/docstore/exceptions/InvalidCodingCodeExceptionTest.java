package uk.nhs.digital.docstore.exceptions;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class InvalidCodingCodeExceptionTest {

    @Test
    public void returnsOperationOutcomeIssueComponentWithExpectedAttributes() {
        InvalidCodingCodeException exception = new InvalidCodingCodeException("path", "code");
        OperationOutcome.OperationOutcomeIssueComponent operationOutcomeIssueComponent = exception.toOperationOutcomeIssue();
        CodeableConcept details = operationOutcomeIssueComponent.getDetails();
        List<Coding> coding = details.getCoding();
        assertThat(operationOutcomeIssueComponent.getCode()).isEqualTo(CODEINVALID);
        assertThat(operationOutcomeIssueComponent.getSeverity()).isEqualTo(ERROR);
        assertThat(coding.get(0).getSystem()).isEqualTo("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1");
        assertThat(coding.get(0).getCode()).isEqualTo("INVALID_CODE_VALUE");
        assertThat(coding.get(0).getDisplay()).isEqualTo("Invalid coding code value");
        assertThat(operationOutcomeIssueComponent.getExpression().get(0).toString()).isEqualTo("path");
    }
}