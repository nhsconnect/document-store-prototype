package uk.nhs.digital.docstore;

import static java.net.http.HttpClient.newHttpClient;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getAwsHost;
import static uk.nhs.digital.docstore.helpers.BaseUriHelper.getBaseUri;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.helpers.AwsS3Helper;

public class DeleteDocumentReferenceE2eTest {
  private static final String BASE_URI_TEMPLATE = "http://%s:%d";
  private static final String AWS_REGION = "eu-west-2";
  private static final int DEFAULT_PORT = 4566;

  private static final String S3_KEY = "abcd";
  private static final String CODE_VALUE = "185361000000102";
  public static final String TABLE_NAME = "DocumentReferenceMetadata";
  String baseUri = String.format(BASE_URI_TEMPLATE, getAwsHost(), DEFAULT_PORT);

  AwsClientBuilder.EndpointConfiguration awsEndpointConfiguration =
      new AwsClientBuilder.EndpointConfiguration(baseUri, AWS_REGION);
  AmazonDynamoDB dynamoDbClient =
      AmazonDynamoDBClientBuilder.standard()
          .withEndpointConfiguration(awsEndpointConfiguration)
          .build();

  AwsS3Helper awsS3 = new AwsS3Helper(awsEndpointConfiguration);

  @BeforeEach
  void setUp() {
    // Delete all items from Metadata table before each test
    ScanResult scanResult = dynamoDbClient.scan("DocumentReferenceMetadata", List.of("ID"));
    scanResult
        .getItems()
        .forEach(item -> dynamoDbClient.deleteItem("DocumentReferenceMetadata", item));
  }

  @Test
  void shouldMarkADocumentsRelatedToTheNhsNumberAsDeletedAndReturnSuccessfulMessage()
      throws IOException, InterruptedException {
    var nhsNumber = "1234567890";
    var s3Location = String.format("s3://%s/%s", awsS3.getDocumentStoreBucketName(), S3_KEY);
    String successfulDeleteResponse = getContentFromResource();
    Map<String, AttributeValue> document =
        Map.of(
            "ID", new AttributeValue("1234"),
            "NhsNumber", new AttributeValue(nhsNumber),
            "Location", new AttributeValue(s3Location),
            "ContentType", new AttributeValue("text/plain"),
            "DocumentUploaded", new AttributeValue().withBOOL(true),
            "FileName", new AttributeValue("uploaded document"),
            "Created", new AttributeValue("2021-11-04T15:57:30Z"),
            "Type", new AttributeValue().withL(new AttributeValue(CODE_VALUE)));

    dynamoDbClient.putItem(TABLE_NAME, document);

    var nhsNumberParameter =
        URLEncoder.encode("https://fhir.nhs.uk/Id/nhs-number|" + nhsNumber, StandardCharsets.UTF_8);
    var deleteDocumentReferenceRequest =
        HttpRequest.newBuilder(
                getBaseUri().resolve("DocumentReference?subject:identifier=" + nhsNumberParameter))
            .DELETE()
            .build();

    var deleteDocumentReferenceResponse =
        newHttpClient().send(deleteDocumentReferenceRequest, HttpResponse.BodyHandlers.ofString());
    var actual = dynamoDbClient.getItem(TABLE_NAME, Map.of("ID", document.get("ID")));
    var deletedAt = actual.getItem().get("Deleted").getS();

    assertThat(deleteDocumentReferenceResponse.statusCode()).isEqualTo(200);
    assertThat(Instant.now().isAfter(Instant.parse(deletedAt))).isTrue();
    assertThatJson(deleteDocumentReferenceResponse.body()).isEqualTo(successfulDeleteResponse);
  }

  private String getContentFromResource() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    File file =
        new File(classLoader.getResource("delete/successful-delete-response.json").getFile());
    return new String(Files.readAllBytes(file.toPath()));
  }
}
