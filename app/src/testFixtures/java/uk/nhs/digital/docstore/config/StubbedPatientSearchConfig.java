package uk.nhs.digital.docstore.config;

import uk.nhs.digital.docstore.patientdetails.PatientSearchConfig;

public class StubbedPatientSearchConfig extends PatientSearchConfig {
    @Override
    public boolean pdsFhirIsStubbed() {
        return true;
    }
}
