package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.helpers.RequestEventBuilder;
import uk.nhs.digital.docstore.patientdetails.FakePdsFhirClient;
import uk.nhs.digital.docstore.patientdetails.SearchPatientDetailsHandler;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchForPatientInlineTest {

    @Mock
    private Context context;
    @Mock
    private AmazonSQS amazonSqsClient;

    @Captor
    ArgumentCaptor<SendMessageRequest> sendMessageRequestArgumentCaptor;

    private SearchPatientDetailsHandler handler;
    private RequestEventBuilder requestBuilder = new RequestEventBuilder();

    @BeforeEach
    public void setUp() {
        StubbedApplication application = new StubbedApplication();
        handler = new SearchPatientDetailsHandler(application);
        requestBuilder = new RequestEventBuilder();
    }

    @Test
    void returnsSuccessResponse() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000009")
                .build();
        var queueUrl = "document-store-audit-queue-url";
        var getQueueRequest = new GetQueueUrlResult().withQueueUrl(queueUrl);

        when(amazonSqsClient.getQueueUrl("document-store-audit")).thenReturn(getQueueRequest);
        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource("search-patient-details/patient-details-response.json"));
    }

    @Test
    void returnsSuccessResponseWithLimitedInformation() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000025")
                .build();
        var queueUrl = "document-store-audit-queue-url";
        var getQueueRequest = new GetQueueUrlResult().withQueueUrl(queueUrl);

        when(amazonSqsClient.getQueueUrl("document-store-audit")).thenReturn(getQueueRequest);
        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(200);
        assertThat(responseEvent.getHeaders().get("Content-Type")).isEqualTo("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta", "$.entry[*].resource.meta")
                .isEqualTo(getContentFromResource("search-patient-details/patient-details-response-for-missing-information.json"));
    }

    @Test
    void returnsMissingPatientResponseWhenPatientNotFound() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9111231130")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(404);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .whenIgnoringPaths("$.meta")
                .isEqualTo(getContentFromResource("search-patient-details/missing-patient-response.json"));
    }

    @Test
    void returnsErrorResponseWhenAnUnrecognisedSubjectIdentifierSystemIsInput() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "unrecognised-subject-identifier-system|9000000009")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/unrecognised-subject-identifier-system.json"));
    }

    @Test
    void returnsErrorResponseWhenAnInvalidSubjectIdentifierIsInput() throws IOException {
        var request = requestBuilder
                .addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|")
                .build();

        var responseEvent = handler.handleRequest(request, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/invalid-subject-identifier.json"));
    }

    @Test
    void returnsErrorResponseWhenSearchParametersAreMissing() throws IOException {
        var parameterlessRequest = requestBuilder.build();

        var responseEvent = handler.handleRequest(parameterlessRequest, context);

        assertThat(responseEvent.getStatusCode()).isEqualTo(400);
        assertThat(responseEvent.getHeaders().get("Content-Type")).contains("application/fhir+json");
        assertThatJson(responseEvent.getBody())
                .isEqualTo(getContentFromResource("errors/missing-search-parameters.json"));
    }

    @Test
    void publishesSensitiveAuditMessageWhenPatientIsFound() throws IOException {
        var searchPatientDetailsRequest = requestBuilder.addQueryParameter("subject:identifier", "https://fhir.nhs.uk/Id/nhs-number|9000000009").build();
        var queueUrl = "document-store-audit-queue-url";
        var getQueueRequest = new GetQueueUrlResult().withQueueUrl(queueUrl);
        var patientDetails = getContentFromResource("search-patient-details/patient-details-response.json");
        var expectedSendMessageRequest = new SendMessageRequest().withQueueUrl(queueUrl).withMessageBody(patientDetails);

        when(amazonSqsClient.getQueueUrl("document-store-audit")).thenReturn(getQueueRequest);
        handler.handleRequest(searchPatientDetailsRequest, context);

        verify(amazonSqsClient, times(1)).sendMessage(sendMessageRequestArgumentCaptor.capture());
        var actualSendMessageRequest = sendMessageRequestArgumentCaptor.getValue();
        assertThatJson(actualSendMessageRequest.equals(expectedSendMessageRequest));
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    public class StubbedApplication extends Application {
        StubbedApplication() {
            this.pdsFhirClient = new FakePdsFhirClient();
            this.auditPublisher = new SplunkPublisher(amazonSqsClient);
        }
    }
}

