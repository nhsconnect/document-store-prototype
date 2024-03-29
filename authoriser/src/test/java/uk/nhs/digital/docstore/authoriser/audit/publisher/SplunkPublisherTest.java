package uk.nhs.digital.docstore.authoriser.audit.publisher;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.authoriser.audit.message.AuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.message.BaseAuditMessage;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemStubsExtension.class)
class SplunkPublisherTest {
    @Mock private AmazonSQS amazonSqsClient;

    @Test
    void sendsMessageToSqsQueue() throws JsonProcessingException {
        var queueUrl = "document-store-audit-queue-url";
        var messageBody = new StubAuditMessage("Audit payload");
        var sendMessageRequest =
                new SendMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMessageBody(messageBody.toJsonString());

        new SplunkPublisher(amazonSqsClient, queueUrl).publish(messageBody);

        verify(amazonSqsClient, times(1)).sendMessage(sendMessageRequest);
    }

    private static class StubAuditMessage extends BaseAuditMessage implements AuditMessage {
        private final String message;

        public StubAuditMessage(String message) {
            this.message = message;
        }

        public String toJsonString() {
            return message;
        }

        @Override
        public String getDescription() {
            return "Something happened";
        }
    }
}
