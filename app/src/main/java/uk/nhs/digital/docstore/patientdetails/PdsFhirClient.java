package uk.nhs.digital.docstore.patientdetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Address;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Name;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Period;

import java.time.LocalDate;
import java.util.List;

public class PdsFhirClient {
    private static final Logger logger = LoggerFactory.getLogger(PdsFhirClient.class);

    private final PatientSearchConfig patientSearchConfig;
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
    }

    public Patient fetchPatientDetails(String nhsNumber) {
        if (patientSearchConfig.pdsFhirIsStubbed()) {
            logger.info("Returning stub PDS adaptor response");
            return getStubbedPatient(nhsNumber);
        }

        var path = "Patient/" + nhsNumber;
        logger.info("Confirming NHS number with PDS adaptor at " + patientSearchConfig.pdsFhirRootUri());
        var response = httpClient.get(patientSearchConfig.pdsFhirRootUri(), path);

        if (response.statusCode() == 200) {
            return Patient.parseFromJson(response.body());
        }
        handleErrorResponse(response.statusCode(), nhsNumber);
        return null;
    }

    private Patient getStubbedPatient(String nhsNumber) {
        var currentPeriod = new Period(LocalDate.now().minusYears(1), null);
        var currentName = new Name(currentPeriod, "usual", List.of("Jane"), "Doe");
        var currentAddress = new Address(currentPeriod, "LS1 6AE", "home");
        return new Patient(nhsNumber, "1998-07-11", List.of(currentAddress), List.of(currentName));
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
