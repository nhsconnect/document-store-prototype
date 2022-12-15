package uk.nhs.digital.docstore.patientdetails;

import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;

public interface PdsFhirClient {
    Patient fetchPatientDetails(String nhsNumber);
}
