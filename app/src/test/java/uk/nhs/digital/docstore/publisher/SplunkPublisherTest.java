package uk.nhs.digital.docstore.publisher;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.auditmessages.AuditMessage;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SplunkPublisherTest {
    @Mock
    private AmazonSQS amazonSqsClient;

    @Test
    void sendsMessageToSqsQueue() throws JsonProcessingException {
        var queueUrl = "document-store-audit-queue-url";
        var messageBody = new StubAuditMessage("Audit payload");
        var getQueueRequest = new GetQueueUrlResult().withQueueUrl(queueUrl);
        var sendMessageRequest = new SendMessageRequest().withQueueUrl(queueUrl).withMessageBody(messageBody.toJsonString());

        when(amazonSqsClient.getQueueUrl("document-store-audit")).thenReturn(getQueueRequest);
        new SplunkPublisher(amazonSqsClient).publish(messageBody);

        verify(amazonSqsClient, times(1)).sendMessage(sendMessageRequest);
    }

    private static class StubAuditMessage implements AuditMessage {
        private final String message;
        public StubAuditMessage(String message) {
            this.message = message;
        }
        public String toJsonString() {
            return message;
        }
    }
}