package uk.nhs.digital.docstore.publisher;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.auditmessages.BaseAuditMessage;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class SplunkPublisherTest {
    @Mock
    private AmazonSQS amazonSqsClient;

    @SuppressWarnings("unused")
    @SystemStub
    private EnvironmentVariables environmentVariables;

    @Test
    void sendsMessageToSqsQueue() throws JsonProcessingException {
        var queueUrl = "document-store-audit-queue-url";
        var messageBody = new StubAuditMessage("Audit payload");
        var sendMessageRequest = new SendMessageRequest().withQueueUrl(queueUrl).withMessageBody(messageBody.toJsonString());

        environmentVariables.set("SQS_QUEUE_URL", queueUrl);
        new SplunkPublisher(amazonSqsClient).publish(messageBody);

        verify(amazonSqsClient, times(1)).sendMessage(sendMessageRequest);
    }

    private static class StubAuditMessage extends BaseAuditMessage {
        private final String message;

        public StubAuditMessage(String message) {
            this.message = message;
        }

        public String toJsonString() {
            return message;
        }
    }
}