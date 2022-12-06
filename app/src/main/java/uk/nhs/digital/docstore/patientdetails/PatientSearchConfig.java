package uk.nhs.digital.docstore.patientdetails;

import uk.nhs.digital.docstore.config.Environment;

public class PatientSearchConfig {

    public static final String TRUE = "true";
    private final Environment environment;

    public PatientSearchConfig() {
        this(new Environment());
    }

    public PatientSearchConfig(Environment environment) {
        this.environment = environment;
    }

    public String pdsAdaptorRootUri() {
        return environment.getEnvVar("PDS_FHIR_ENDPOINT", "https://sandbox.api.service.nhs.uk/personal-demographics/FHIR/R4/");
    }

    public boolean pdsAdaptorIsStubbed() {
        return TRUE.equals(environment.getEnvVar("PDS_ADAPTOR_IS_STUBBED", TRUE));
    }

}
