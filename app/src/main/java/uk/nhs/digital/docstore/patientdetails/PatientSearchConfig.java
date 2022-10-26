package uk.nhs.digital.docstore.patientdetails;

public class PatientSearchConfig {
    public String pdsAdaptorRootUri() {
        return System.getenv().getOrDefault("PDS_ADAPTOR_BASE_URL", "http://pds-adaptor:8080/");
    }
}
