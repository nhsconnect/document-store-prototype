package uk.nhs.digital.docstore.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.auditmessages.AuditMessage;

public class SplunkPublisher implements AuditPublisher {
    private final AmazonSQS amazonSqsClient;

    public SplunkPublisher() {
        this(AmazonSQSClientBuilder.defaultClient());
    }

    public SplunkPublisher(AmazonSQS amazonSqsClient) {
        this.amazonSqsClient = amazonSqsClient;
    }

    public void publish(AuditMessage auditMessage) throws JsonProcessingException {
        var queueUrl = amazonSqsClient.getQueueUrl("document-store-audit").getQueueUrl();
        var messageRequest = new SendMessageRequest().withQueueUrl(queueUrl).withMessageBody(auditMessage.toJsonString());

        amazonSqsClient.sendMessage(messageRequest);
    }
}
