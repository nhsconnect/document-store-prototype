package uk.nhs.digital.docstore.audit.publisher;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.audit.message.AuditMessage;

public class SplunkPublisher implements AuditPublisher {
  private static final String AWS_REGION = "eu-west-2";
  private static final String DEFAULT_ENDPOINT = "";
  private static final String SQS_ENDPOINT_ENV = "SQS_ENDPOINT";

  private final AmazonSQS amazonSqsClient;

  public SplunkPublisher() {
    var clientBuilder = AmazonSQSClientBuilder.standard();
    var sqsEndpoint = System.getenv(SQS_ENDPOINT_ENV);

    if (!sqsEndpoint.equals(DEFAULT_ENDPOINT)) {
      var endpointConfiguration =
          new AwsClientBuilder.EndpointConfiguration(sqsEndpoint, AWS_REGION);
      clientBuilder = clientBuilder.withEndpointConfiguration(endpointConfiguration);
    }

    amazonSqsClient = clientBuilder.build();
  }

  public SplunkPublisher(AmazonSQS amazonSqsClient) {
    this.amazonSqsClient = amazonSqsClient;
  }

  public void publish(AuditMessage auditMessage) throws JsonProcessingException {
    var queueUrl = System.getenv("SQS_QUEUE_URL");
    var messageRequest =
        new SendMessageRequest()
            .withQueueUrl(queueUrl)
            .withMessageBody(auditMessage.toJsonString());

    amazonSqsClient.sendMessage(messageRequest);
  }
}
