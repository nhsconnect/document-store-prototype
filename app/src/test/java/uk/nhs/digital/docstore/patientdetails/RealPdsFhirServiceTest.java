package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.auditmessages.SearchPatientDetailsAuditMessage;
import uk.nhs.digital.docstore.exceptions.InvalidResourceIdException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.logs.TestLogAppender;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealPdsFhirServiceTest {
    @Mock
    private SimpleHttpClient httpClient;
    @Mock
    private SplunkPublisher splunkPublisher;

    @Test
    public void shouldMakeObservedCallToPdsAndReturnPatientDetailsWhenPdsFhirReturns200() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var now = Instant.now();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, now);
        var nhsNumber = "9000000009";
        var auditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 200, now);

        when(httpClient.get(any(), any())).thenReturn(new StubPdsResponse(200, getJSONPatientDetails(nhsNumber)));
        pdsFhirClient.fetchPatientDetails(nhsNumber);

        verify(httpClient).get(any(), contains(nhsNumber));
        verify(splunkPublisher).publish(refEq(auditMessage));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    public void shouldMakeObservedCallToPdsAndThrowExceptionWhenPdsFhirReturns400() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var now = Instant.now();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, now);
        var nhsNumber = "9000000000";
        var auditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 400, now);

        when(httpClient.get(any(), any())).thenReturn(new StubPdsResponse(400, null));
        assertThrows(InvalidResourceIdException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber));

        verify(httpClient).get(any(), contains(nhsNumber));
        verify(splunkPublisher).publish(refEq(auditMessage));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    public void shouldMakeObservedCallToPdsAndThrowExceptionWhenPdsFhirReturns404() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var now = Instant.now();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, now);
        var nhsNumber = "9111231130";
        var auditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 404, now);

        when(httpClient.get(any(), any())).thenReturn(new StubPdsResponse(404, null));
        assertThrows(PatientNotFoundException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber), "Patient does not exist for given NHS number.");

        verify(httpClient).get(any(), contains(nhsNumber));
        verify(splunkPublisher).publish(refEq(auditMessage));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsFhirRootUri())).isNotNull();
    }

    @Test
    public void shouldMakeObservedCallToPdsAndThrowExceptionWhenPdsFhirReturnsAnyOtherErrorCode() throws JsonProcessingException {
        var testLogappender = TestLogAppender.addTestLogAppender();
        var now = Instant.now();
        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsFhirClient = new RealPdsFhirService(stubbingOffPatientSearchConfig, httpClient, splunkPublisher, now);
        var nhsNumber = "9111231130";
        var auditMessage = new SearchPatientDetailsAuditMessage(nhsNumber, 500, now);

        when(httpClient.get(any(), any())).thenReturn(new StubPdsResponse(500, null));
        assertThrows(RuntimeException.class, () -> pdsFhirClient.fetchPatientDetails(nhsNumber), "Got an error when requesting patient from PDS: 500");

        verify(httpClient).get(any(), contains(nhsNumber));
        verify(splunkPublisher).publish(refEq(auditMessage));
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