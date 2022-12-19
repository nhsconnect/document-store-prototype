package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.auditmessages.PatientSearchAuditMessage;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;
import uk.nhs.digital.docstore.publishers.AuditPublisher;

import java.time.Instant;

public class RealPdsFhirService implements PdsFhirService {
    private static final Logger logger = LoggerFactory.getLogger(RealPdsFhirService.class);

    private final PatientSearchConfig patientSearchConfig;
    private final SimpleHttpClient httpClient;
    private final AuditPublisher sensitiveIndex;
    private final Instant now;

    public RealPdsFhirService(PatientSearchConfig patientSearchConfig, AuditPublisher auditPublisher) {
        this(patientSearchConfig, new SimpleHttpClient(), auditPublisher, Instant.now());
    }

    public RealPdsFhirService(PatientSearchConfig patientSearchConfig, SimpleHttpClient httpClient, AuditPublisher sensitiveIndex, Instant now) {
        this.patientSearchConfig = patientSearchConfig;
        this.httpClient = httpClient;
        this.sensitiveIndex = sensitiveIndex;
        this.now = now;
    }

    public Patient fetchPatientDetails(String nhsNumber) throws JsonProcessingException {
        var path = "Patient/" + nhsNumber;

        logger.info("Confirming NHS number with PDS adaptor at " + patientSearchConfig.pdsFhirRootUri());
        var response = httpClient.get(patientSearchConfig.pdsFhirRootUri(), path);

        sensitiveIndex.publish(new PatientSearchAuditMessage(nhsNumber, response.statusCode(), now));

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
