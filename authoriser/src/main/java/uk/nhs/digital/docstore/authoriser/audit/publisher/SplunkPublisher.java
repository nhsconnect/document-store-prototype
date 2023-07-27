package uk.nhs.digital.docstore.authoriser.audit.publisher;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.audit.message.AuditMessage;

public class SplunkPublisher implements AuditPublisher {
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";
    private static final String SQS_ENDPOINT_ENV = "SQS_ENDPOINT";

    private final AmazonSQS amazonSqsClient;
    private final String queueUrl;

    private static final Logger LOGGER = LoggerFactory.getLogger(SplunkPublisher.class);

    public SplunkPublisher(String queueUrl) {
        this.queueUrl = queueUrl;
        var clientBuilder = AmazonSQSClientBuilder.standard();
        var sqsEndpoint = System.getenv(SQS_ENDPOINT_ENV);

        if (!sqsEndpoint.equals(DEFAULT_ENDPOINT)) {
            var endpointConfiguration =
                    new AwsClientBuilder.EndpointConfiguration(sqsEndpoint, AWS_REGION);
            clientBuilder = clientBuilder.withEndpointConfiguration(endpointConfiguration);
        }

        amazonSqsClient = clientBuilder.build();
    }

    public SplunkPublisher(AmazonSQS amazonSqsClient, String queueUrl) {
        this.amazonSqsClient = amazonSqsClient;
        this.queueUrl = queueUrl;
    }

    public void publish(AuditMessage auditMessage) throws JsonProcessingException {
        var messageRequest =
                new SendMessageRequest()
                        .withQueueUrl(queueUrl)
                        .withMessageBody(auditMessage.toJsonString());
        amazonSqsClient.sendMessage(messageRequest);
    }
}
