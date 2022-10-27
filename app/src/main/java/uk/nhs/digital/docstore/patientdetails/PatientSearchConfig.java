package uk.nhs.digital.docstore.patientdetails;

public class PatientSearchConfig {

    public static final String TRUE = "true";

    public String pdsAdaptorRootUri() {
        return getEnvVar("PDS_ADAPTOR_BASE_URL", "http://pds-adaptor:8080/");
    }

    public boolean pdsAdaptorIsStubbed() {
        return TRUE.equals(getEnvVar("PDS_ADAPTOR_IS_STUBBED", TRUE));
    }

    private static String getEnvVar(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
