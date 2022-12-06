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
            logger.info("Returning stub PDS adaptor response");
            return new PatientDetails(List.of("Jane"), "Doe", "1998-07-11", "LS1 6AE", nhsNumber);
        }


        String path = "Patient/" + nhsNumber;
        logger.info("Confirming NHS number with PDS adaptor at " + patientSearchConfig.pdsAdaptorRootUri());
        var response = httpClient.get(patientSearchConfig.pdsAdaptorRootUri(), path);

        PatientDetails patientDetails = null;
        if (response.statusCode() != 404) {
            patientDetails = patientDetailsMapper.fromPatientDetailsResponseBody(response.body());
        }
        return patientDetails;
    }

}
