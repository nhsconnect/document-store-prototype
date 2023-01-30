package uk.nhs.digital.docstore.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;

import java.util.List;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;

public class UnrecognisedCodingSystemExceptionTest {

    @Test
    public void returnsOperationOutcomeIssueComponentWithExpectedAttributes() {
        UnrecognisedCodingSystemException exception =
                new UnrecognisedCodingSystemException("coding-system");
        OperationOutcome.OperationOutcomeIssueComponent operationOutcomeIssueComponent =
                exception.toOperationOutcomeIssue();
        CodeableConcept details = operationOutcomeIssueComponent.getDetails();
        List<Coding> coding = details.getCoding();
        assertThat(operationOutcomeIssueComponent.getCode()).isEqualTo(CODEINVALID);
        assertThat(operationOutcomeIssueComponent.getSeverity()).isEqualTo(ERROR);
        assertThat(coding.get(0).getSystem())
                .isEqualTo("https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1");
        assertThat(coding.get(0).getCode()).isEqualTo("INVALID_CODE_SYSTEM");
        assertThat(coding.get(0).getDisplay()).isEqualTo("Invalid code system");
    }
}
