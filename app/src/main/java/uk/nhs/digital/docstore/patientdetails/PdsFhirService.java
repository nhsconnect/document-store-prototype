package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.config.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;

public interface PdsFhirService {
    Patient fetchPatientDetails(String nhsNumber) throws JsonProcessingException, MissingEnvironmentVariableException;
}
