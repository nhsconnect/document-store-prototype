package uk.nhs.digital.docstore.patientdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PdsAdaptorClient {
    private static final Logger logger = LoggerFactory.getLogger(PdsAdaptorClient.class);

    private final PatientSearchConfig patientSearchConfig;
    private final PatientDetailsMapper patientDetailsMapper;
    private final SimpleHttpClient httpClient;

    public PdsAdaptorClient() {
        this(new PatientSearchConfig());
    }

    public PdsAdaptorClient(PatientSearchConfig patientSearchConfig) {
        this.patientSearchConfig = patientSearchConfig;
        this.patientDetailsMapper = new PatientDetailsMapper();
        this.httpClient = new SimpleHttpClient();
    }

    public PatientDetails fetchPatientDetails(String nhsNumber) {
        logger.debug("Confirming NHS number with PDS adaptor");

        String path = "patient-trace-information/" + nhsNumber;
        var response = httpClient.get(patientSearchConfig.pdsAdaptorRootUri(), path);

        PatientDetails patientDetails = null;
        if (response.statusCode() != 404) {
            patientDetails = patientDetailsMapper.fromPatientDetailsResponseBody(response.body());
        }
        return patientDetails;
    }

}
