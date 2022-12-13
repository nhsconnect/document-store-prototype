package uk.nhs.digital.docstore.exceptions;

import org.hl7.fhir.r4.model.OperationOutcome;

import static org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.r4.model.OperationOutcome.IssueType.CODEINVALID;

public class TestException extends RuntimeException implements OperationOutcomeIssuable {
    public TestException(String message) {
        super(message);
    }

    @Override
    public OperationOutcome.OperationOutcomeIssueComponent toOperationOutcomeIssue() {
        return new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity(ERROR)
                .setCode(CODEINVALID);
    }
}
