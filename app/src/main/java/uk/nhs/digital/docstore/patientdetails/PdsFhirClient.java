package uk.nhs.digital.docstore.patientdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PdsFhirClient {
    private static final Logger logger = LoggerFactory.getLogger(PdsFhirClient.class);

    private final PatientSearchConfig patientSearchConfig;
    private final PatientDetailsMapper patientDetailsMapper;
    private final SimpleHttpClient httpClient;

    public PdsFhirClient() {
        this(new PatientSearchConfig());
    }

    public PdsFhirClient(PatientSearchConfig patientSearchConfig) {
        this(patientSearchConfig, new SimpleHttpClient());
    }

    public PdsFhirClient(PatientSearchConfig patientSearchConfig, SimpleHttpClient httpClient) {
        this.patientSearchConfig = patientSearchConfig;
        this.httpClient = httpClient;
        this.patientDetailsMapper = new PatientDetailsMapper();
    }

    public PatientDetails fetchPatientDetails(String nhsNumber) {
        if (patientSearchConfig.pdsFhirIsStubbed()) {
            logger.info("Returning stub PDS adaptor response");
            return new PatientDetails(List.of("Jane"), "Doe", "1998-07-11", "LS1 6AE", nhsNumber);
        }


        var path = "Patient/" + nhsNumber;
        logger.info("Confirming NHS number with PDS adaptor at " + patientSearchConfig.pdsFhirRootUri());
        var response = httpClient.get(patientSearchConfig.pdsFhirRootUri(), path);

        PatientDetails patientDetails = null;
        if (response.statusCode() != 404) {
            patientDetails = patientDetailsMapper.fromPatientDetailsResponseBody(response.body());
        }
        return patientDetails;
    }

}
