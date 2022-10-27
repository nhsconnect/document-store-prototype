package uk.nhs.digital.docstore.patientdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PdsAdaptorClient {
    private static final Logger logger = LoggerFactory.getLogger(PdsAdaptorClient.class);

    private final PatientSearchConfig patientSearchConfig;
    private final PatientDetailsMapper patientDetailsMapper;
    private final SimpleHttpClient httpClient;

    public PdsAdaptorClient() {
        this(new PatientSearchConfig());
    }

    public PdsAdaptorClient(PatientSearchConfig patientSearchConfig) {
        this(patientSearchConfig, new SimpleHttpClient());
    }

    public PdsAdaptorClient(PatientSearchConfig patientSearchConfig, SimpleHttpClient httpClient) {
        this.patientSearchConfig = patientSearchConfig;
        this.httpClient = httpClient;
        this.patientDetailsMapper = new PatientDetailsMapper();
    }

    public PatientDetails fetchPatientDetails(String nhsNumber) {
        if (patientSearchConfig.pdsAdaptorIsStubbed()) {
            return new PatientDetails(List.of("bob"), "gibbons", "1980-10-14", "M1ME", nhsNumber);
        }
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
