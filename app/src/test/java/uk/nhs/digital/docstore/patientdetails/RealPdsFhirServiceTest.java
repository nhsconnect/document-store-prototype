package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.audit.message.SearchPatientDetailsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.MissingEnvironmentVariableException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.logs.TestLogAppender;
import uk.nhs.digital.docstore.model.BirthDate;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;
import uk.nhs.digital.docstore.model.PatientName;
import uk.nhs.digital.docstore.model.Postcode;
import uk.nhs.digital.docstore.patientdetails.auth.AuthService;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealPdsFhirServiceTest {
    @Mock
    private SimpleHttpClient httpClient;
    @Mock
    private SplunkPublisher splunkPublisher;
    @Mock
    private AuthService authService;
    @Mock
    private PatientSearchConfig patientSearchConfig;
    @Captor
    private ArgumentCaptor<SearchPatientDetailsAuditMessage> sensitiveAuditMessageCaptor;

    @BeforeEach
    void setUp() throws MissingEnvironmentVariableException {
        when(patientSearchConfig.pdsFhirRootUri()).thenReturn("pds-fhir-endpoint");
    }

    @Test
    void makesObservedCallToPdsAndReturnPatientDetailsWhenPdsFhirReturns200() throws JsonProcessingException, MissingEnvironmentVariableException, IllFormedPatientDetailsException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var pdsFhirClient = new RealPdsFhirService(patientSearchConfig, httpClient, splunkPublisher, authService);
        NhsNumber nhsNumber = new NhsNumber("9000000009");
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 200);
        var accessToken = "token";
        var expectedPatient = new PatientDetails(List.of(new PatientName("Jane")), new PatientName("Doe"),new BirthDate("Test"), new Postcode("EX1 2EX"), nhsNumber);

        when(httpClient.get(any(), any(), eq(accessToken)))
                .thenReturn(new StubPdsResponse(200, getJSONPatientDetails(nhsNumber)));
        when(authService.retrieveAccessToken()).thenReturn(accessToken);
        var patient = pdsFhirClient.fetchPatientDetails(nhsNumber);

        verify(httpClient).get(any(), contains(nhsNumber.getValue()), eq(accessToken));
        assertThat(patient).usingRecursiveComparison().isEqualTo(expectedPatient);
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(patientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    void makesObservedCallToPdsAndThrowExceptionWhenPdsFhirReturns400() throws JsonProcessingException, MissingEnvironmentVariableException, IllFormedPatientDetailsException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var pdsFhirClient = new RealPdsFhirService(patientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = new NhsNumber("9000000000");
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 400);
        var accessToken = "token";

        when(httpClient.get(any(), any(), eq(accessToken))).thenReturn(new StubPdsResponse(400, null));
        when(authService.retrieveAccessToken()).thenReturn(accessToken);
        assertThrows(InvalidResourceIdException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber));

        verify(httpClient).get(any(), contains(nhsNumber.getValue()), eq(accessToken));
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(patientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    void makesObservedCallToPdsAndThrowExceptionWhenPdsFhirReturns404() throws JsonProcessingException, MissingEnvironmentVariableException, IllFormedPatientDetailsException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var pdsFhirClient = new RealPdsFhirService(patientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = new NhsNumber("9111231130");
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 404);
        var accessToken = "token";

        when(httpClient.get(any(), any(), eq(accessToken))).thenReturn(new StubPdsResponse(404, null));
        when(authService.retrieveAccessToken()).thenReturn(accessToken);

        assertThrows(PatientNotFoundException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber), "Patient does not exist for given NHS number.");

        verify(httpClient).get(any(), contains(nhsNumber.getValue()), eq(accessToken));
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(patientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    void makesObservedCallToPdsAndThrowExceptionWhenPdsFhirReturnsAnyOtherErrorCode() throws JsonProcessingException, MissingEnvironmentVariableException, IllFormedPatientDetailsException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var pdsFhirClient = new RealPdsFhirService(patientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = new NhsNumber("9111231130");
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 500);
        var accessToken = "token";

        when(httpClient.get(any(), any(), eq(accessToken))).thenReturn(new StubPdsResponse(500, null));
        when(authService.retrieveAccessToken()).thenReturn(accessToken);
        assertThrows(RuntimeException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber), "Got an error when requesting patient from PDS: 500");

        verify(httpClient).get(any(), contains(nhsNumber.getValue()), eq(accessToken));
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(patientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    void makesCallToGetAccessTokenTwiceIfTokenHasExpired() throws MissingEnvironmentVariableException, JsonProcessingException, IllFormedPatientDetailsException {
        var pdsFhirClient = new RealPdsFhirService(patientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = new NhsNumber("9000000009");
        var accessToken = "token";
        var expectedPatient = new PatientDetails(List.of(new PatientName("Jane")), new PatientName("Doe"),new BirthDate("Test"), new Postcode("EX1 2EX"), nhsNumber);

        when(httpClient.get(any(), any(), eq(accessToken)))
                .thenReturn(new StubPdsResponse(401, "some error"), new StubPdsResponse(200, getJSONPatientDetails(nhsNumber)));
        when(authService.retrieveAccessToken()).thenReturn(accessToken);
        when(authService.getNewAccessToken()).thenReturn(accessToken);

        var patient = pdsFhirClient.fetchPatientDetails(nhsNumber);

        verify(httpClient, times(2)).get(any(), any(), eq(accessToken));
        verify(splunkPublisher, times(2)).publish(any());
        assertThat(patient).usingRecursiveComparison().isEqualTo(expectedPatient);
    }

    private String getJSONPatientDetails(NhsNumber nhsNumber) {
        var jsonPeriod = new JSONObject()
                .put("start", LocalDate.now().minusYears(1))
                .put("end", JSONObject.NULL);
        var jsonName = new JSONObject()
                .put("period", jsonPeriod)
                .put("use", "usual")
                .put("given", List.of("Jane"))
                .put("family", "Doe");
        var jsonAddress = new JSONObject()
                .put("period", jsonPeriod)
                .put("use", "home")
                .put("postalCode", "EX1 2EX");

        return new JSONObject()
                .put("name", List.of(jsonName))
                .put("birthDate", "Test")
                .put("address", List.of(jsonAddress))
                .put("id", nhsNumber.getValue()).toString();
    }

    private static class StubPdsResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;

        public StubPdsResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int statusCode() {
            return statusCode;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return null;
        }

        @Override
        public HttpClient.Version version() {
            return null;
        }
    }
}