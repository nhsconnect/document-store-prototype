package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;

public interface PdsFhirService {
    PatientDetails fetchPatientDetails(NhsNumber nhsNumber) throws JsonProcessingException, MissingEnvironmentVariableException, IllFormedPatientDetailsException;
}
