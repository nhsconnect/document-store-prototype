package uk.nhs.digital.docstore.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SplunkPublisher implements AuditPublisher {
    private final AmazonSQS amazonSqsClient;

    public SplunkPublisher() {
        this(AmazonSQSClientBuilder.defaultClient());
    }

    public SplunkPublisher(AmazonSQS amazonSqsClient) {
        this.amazonSqsClient = amazonSqsClient;
    }

    public void publish(String auditMessage) {
        var queueUrl = amazonSqsClient.getQueueUrl("document-store-audit").getQueueUrl();
        var messageRequest = new SendMessageRequest().withQueueUrl(queueUrl).withMessageBody(auditMessage);

        amazonSqsClient.sendMessage(messageRequest);
    }
}
