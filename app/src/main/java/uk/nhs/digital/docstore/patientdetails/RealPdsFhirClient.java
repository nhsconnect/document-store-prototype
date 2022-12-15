package uk.nhs.digital.docstore.patientdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;

public class RealPdsFhirClient implements PdsFhirClient {
    private static final Logger logger = LoggerFactory.getLogger(RealPdsFhirClient.class);

    private final PatientSearchConfig patientSearchConfig;
    private final SimpleHttpClient httpClient;

    public RealPdsFhirClient(PatientSearchConfig patientSearchConfig) {
        this(patientSearchConfig, new SimpleHttpClient());
    }

    public RealPdsFhirClient(PatientSearchConfig patientSearchConfig, SimpleHttpClient httpClient) {
        this.patientSearchConfig = patientSearchConfig;
        this.httpClient = httpClient;
    }

    public Patient fetchPatientDetails(String nhsNumber) {
        var path = "Patient/" + nhsNumber;

        logger.info("Confirming NHS number with PDS adaptor at " + patientSearchConfig.pdsFhirRootUri());
        var response = httpClient.get(patientSearchConfig.pdsFhirRootUri(), path);

        if (response.statusCode() == 200) {
            return Patient.parseFromJson(response.body());
        }
        handleErrorResponse(response.statusCode(), nhsNumber);
        return null;
    }

    private void handleErrorResponse(int statusCode, String nhsNumber) {
        if (statusCode == 400){
            throw new InvalidResourceIdException(nhsNumber);
        }
        if (statusCode == 404){
            throw new PatientNotFoundException("Patient does not exist for given NHS number.");
        }
        throw new RuntimeException("Got an error when requesting patient from PDS: " + statusCode);
    }

}
