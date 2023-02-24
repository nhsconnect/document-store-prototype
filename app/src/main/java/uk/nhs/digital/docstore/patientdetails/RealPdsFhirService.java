package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.audit.message.SearchPatientDetailsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;
import uk.nhs.digital.docstore.patientdetails.auth.AuthService;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;

import java.net.http.HttpResponse;

public class RealPdsFhirService implements PdsFhirService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealPdsFhirService.class);

    private final PatientSearchConfig patientSearchConfig;
    private final SimpleHttpClient httpClient;
    private final AuditPublisher sensitiveIndex;
    private final AuthService authService;

    public RealPdsFhirService(
            PatientSearchConfig patientSearchConfig,
            AuditPublisher auditPublisher,
            AuthService authService) {
        this(patientSearchConfig, new SimpleHttpClient(), auditPublisher, authService);
    }

    public RealPdsFhirService(
            PatientSearchConfig patientSearchConfig,
            SimpleHttpClient httpClient,
            AuditPublisher sensitiveIndex,
            AuthService authService) {
        this.patientSearchConfig = patientSearchConfig;
        this.httpClient = httpClient;
        this.sensitiveIndex = sensitiveIndex;
        this.authService = authService;
    }

    public PatientDetails fetchPatientDetails(NhsNumber nhsNumber)
            throws JsonProcessingException, MissingEnvironmentVariableException,
            IllFormedPatientDetailsException {
        var accessToken = authService.retrieveAccessToken();

        var pdsResponse = makeRequestWithPdsAndSendAuditMessage(accessToken, nhsNumber);
        boolean expiredAccessToken = pdsResponse.statusCode() == 401;

        if (expiredAccessToken) {
            var newAccessToken = authService.getNewAccessToken();
            var newPdsResponse = makeRequestWithPdsAndSendAuditMessage(newAccessToken, nhsNumber);

            return handleResponse(newPdsResponse, nhsNumber);
        }

        return handleResponse(pdsResponse, nhsNumber);
    }

    private PatientDetails handleResponse(HttpResponse<String> pdsResponse, NhsNumber nhsNumber)
            throws IllFormedPatientDetailsException {
        var statusCode = pdsResponse.statusCode();

        if (pdsResponse.statusCode() == 200) {
            var fhirPatient = Patient.parseFromJson(pdsResponse.body());
            var patientDetails = fhirPatient.parse(nhsNumber);

            if (patientDetails.isSuperseded()) {
                LOGGER.info("Patient is superseded");
            }

            return fhirPatient.parse(nhsNumber);
        }

        if (statusCode == 400) {
            throw new InvalidResourceIdException(nhsNumber);
        }

        if (statusCode == 404) {
            throw new PatientNotFoundException("Patient does not exist for given NHS number.");
        }

        throw new RuntimeException("Got an error when requesting patient from PDS: " + statusCode);
    }

    private HttpResponse<String> makeRequestWithPdsAndSendAuditMessage(
            String accessToken, NhsNumber nhsNumber)
            throws JsonProcessingException, MissingEnvironmentVariableException {
        LOGGER.info(
                "Confirming NHS number with PDS adaptor at "
                        + patientSearchConfig.pdsFhirRootUri());
        var path = "Patient/" + nhsNumber.getValue();
        var response = httpClient.get(patientSearchConfig.pdsFhirRootUri(), path, accessToken);
        sensitiveIndex.publish(
                new SearchPatientDetailsAuditMessage(nhsNumber, response.statusCode()));

        return response;
    }
}
