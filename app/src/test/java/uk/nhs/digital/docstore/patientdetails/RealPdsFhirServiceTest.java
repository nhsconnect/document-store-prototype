package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.auditmessages.SearchPatientDetailsAuditMessage;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.logs.TestLogAppender;
import uk.nhs.digital.docstore.patientdetails.auth.AuthService;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealPdsFhirServiceTest {
    @Mock
    private SimpleHttpClient httpClient;
    @Mock
    private SplunkPublisher splunkPublisher;
    @Mock
    private AuthService authService;
    @Captor
    private ArgumentCaptor<SearchPatientDetailsAuditMessage> sensitiveAuditMessageCaptor;

    @Test
    void makesObservedCallToPdsAndReturnPatientDetailsWhenPdsFhirReturns200() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = "9000000009";
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 200);
        var accessToken = "token";

        when(httpClient.get(any(), any(), eq(accessToken)))
                .thenReturn(new StubPdsResponse(200, getJSONPatientDetails(nhsNumber)));
        when(authService.getAccessToken()).thenReturn(accessToken);
        pdsFhirClient.fetchPatientDetails(nhsNumber);

        verify(httpClient).get(any(), contains(nhsNumber), eq(accessToken));
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    void makesObservedCallToPdsAndThrowExceptionWhenPdsFhirReturns400() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = "9000000000";
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 400);
        var accessToken = "token";

        when(httpClient.get(any(), any(), eq(accessToken))).thenReturn(new StubPdsResponse(400, null));
        when(authService.getAccessToken()).thenReturn(accessToken);
        assertThrows(InvalidResourceIdException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber));

        verify(httpClient).get(any(), contains(nhsNumber), eq(accessToken));
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    void makesObservedCallToPdsAndThrowExceptionWhenPdsFhirReturns404() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = "9111231130";
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 404);
        var accessToken = "token";

        when(httpClient.get(any(), any(), eq(accessToken))).thenReturn(new StubPdsResponse(404, null));
        when(authService.getAccessToken()).thenReturn(accessToken);

        assertThrows(PatientNotFoundException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber), "Patient does not exist for given NHS number.");

        verify(httpClient).get(any(), contains(nhsNumber), eq(accessToken));
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp())
                .isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    void makesObservedCallToPdsAndThrowExceptionWhenPdsFhirReturnsAnyOtherErrorCode() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, authService);
        var nhsNumber = "9111231130";
        var expectedSensitiveAuditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 500);
        var accessToken = "token";

        when(httpClient.get(any(), any(), eq(accessToken))).thenReturn(new StubPdsResponse(500, null));
        when(authService.getAccessToken()).thenReturn(accessToken);
        assertThrows(RuntimeException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber), "Got an error when requesting patient from PDS: 500");

        verify(httpClient).get(any(), contains(nhsNumber), eq(accessToken));
        verify(splunkPublisher).publish(sensitiveAuditMessageCaptor.capture());
        var actualSensitiveAuditMessage = sensitiveAuditMessageCaptor.getValue();
        assertThat(actualSensitiveAuditMessage)
                .usingRecursiveComparison()
                .ignoringFields("timestamp")
                .isEqualTo(expectedSensitiveAuditMessage);
        assertThat(actualSensitiveAuditMessage.getTimestamp()).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    private String getJSONPatientDetails(String nhsNumber) {
        var jsonPeriod = new JSONObject()
                .put("start", LocalDate.now().minusYears(1).toString())
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
                .put("id", nhsNumber).toString();
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

    private static class StubbingOffPatientSearchConfig extends PatientSearchConfig {
        @Override
        public boolean pdsFhirIsStubbed() {
            return false;
        }
    }
}