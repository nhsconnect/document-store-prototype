package uk.nhs.digital.docstore.patientdetails;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.logs.TestLogAppender;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdsAdaptorClientTest {

    @Mock
    private SimpleHttpClient httpClient;

    @Test
    public void shouldReturnStubbedPatientWithoutOutboundHttpCallByDefault() {
        var testLogAppender = TestLogAppender.addTestLogAppender();

        var defaultPatientSearchConfig = new PatientSearchConfig();

        var pdsAdaptorClient = new PdsAdaptorClient(defaultPatientSearchConfig, httpClient);

        var nhsNumber = "123445678";

        var patientDetails = pdsAdaptorClient.fetchPatientDetails(nhsNumber);

        verify(httpClient, never()).get(any(), any());

        assertThat(patientDetails.getNhsNumber()).isEqualTo(nhsNumber);

        assertThat(testLogAppender.findLoggedEvent("stub")).isNotNull();
    }

    @Test
    public void shouldMakeCallPdsAndReturnNullPatientDetailsIfPatientSearchConfigStubbingToggledOff() {
        var testLogappender = TestLogAppender.addTestLogAppender();

        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsAdaptorClient = new PdsAdaptorClient(stubbingOffPatientSearchConfig, httpClient);

        when(httpClient.get(any(), any())).thenReturn(new StubPdsResponse(404, null));

        String nhsNumber = "1234";

        pdsAdaptorClient.fetchPatientDetails(nhsNumber);

        verify(httpClient).get(any(), contains(nhsNumber));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsAdaptorRootUri())).isNotNull();
    }

    @Test
    @Disabled
    public void shouldMakeCallPdsAndReturnNotNullPatientDetailsIfPatientSearchConfigStubbingToggledOff() {
        var testLogappender = TestLogAppender.addTestLogAppender();

        var stubbingOffPatientSearchConfig = new StubbingOffPatientSearchConfig();
        var pdsAdaptorClient = new PdsAdaptorClient(stubbingOffPatientSearchConfig, httpClient);

        when(httpClient.get(any(), any())).thenReturn(new StubPdsResponse(200, this.getJSONPatientDetails()));

        String nhsNumber = "1234";

        pdsAdaptorClient.fetchPatientDetails(nhsNumber);

        verify(httpClient).get(any(), contains(nhsNumber));
        assertThat(testLogappender.findLoggedEvent(stubbingOffPatientSearchConfig.pdsAdaptorRootUri())).isNotNull();
    }

    private String getJSONPatientDetails() {
        return null;
    }

    private static class StubPdsResponse implements HttpResponse<String> {

        private int statusCode;

        private String body;

        public StubPdsResponse(int statusCode, String body) {
            this.statusCode = statusCode;
        }

        @Override
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

    private class StubbingOffPatientSearchConfig extends PatientSearchConfig {
        @Override
        public boolean pdsAdaptorIsStubbed() {
            return false;
        }
    }
}