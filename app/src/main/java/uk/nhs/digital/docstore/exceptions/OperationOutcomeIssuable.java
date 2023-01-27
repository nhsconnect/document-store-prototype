package uk.nhs.digital.docstore.exceptions;

import org.hl7.fhir.r4.model.OperationOutcome;

public interface OperationOutcomeIssuable {

  public OperationOutcome.OperationOutcomeIssueComponent toOperationOutcomeIssue();
}
