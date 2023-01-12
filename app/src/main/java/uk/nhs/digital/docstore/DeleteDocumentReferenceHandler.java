package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.utils.CommonUtils;


public class DeleteDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDocumentReferenceHandler.class);
    private final ApiConfig apiConfig;

    private final CommonUtils utils = new CommonUtils();
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    private final AmazonS3 s3client;

    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";

    public DeleteDocumentReferenceHandler() {
        this(new ApiConfig(), CommonUtils.buildS3Client(DEFAULT_ENDPOINT, AWS_REGION));
    }

    public DeleteDocumentReferenceHandler(ApiConfig apiConfig, AmazonS3 s3client) {
        this.apiConfig = apiConfig;
        this.s3client = s3client;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        LOGGER.debug("API Gateway event received - processing starts");

        try {
            var nhsNumber = utils.getNhsNumberFrom(requestEvent.getQueryStringParameters());

            var documentMetadataList = metadataStore.findByNhsNumber(nhsNumber);
            if (documentMetadataList != null) {
                LOGGER.debug("Deleting document metadata from DynamoDB");
                metadataStore.deleteAndSave(documentMetadataList);

                LOGGER.debug("Deleting documents from S3");
                documentMetadataList.forEach(documentMetadata -> {
                    var bucketName = documentMetadata.getLocation().split("//")[1].split("/")[0];
                    var objectKey = documentMetadata.getLocation().split("//")[1].split("/")[1];
                    LOGGER.debug("Deleting object key: " + objectKey + "from bucket: " + bucketName);
                    s3client.deleteObject(bucketName, objectKey);
                });
            }

            LOGGER.debug("Processing finished - about to return the response");
            var body = getJsonBody();
            return apiConfig.getApiGatewayResponse(200, body, "DELETE", null);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e);
        }
    }

    private String getJsonBody() {
        return "{\n" +
                "   \"result\": {\n" +
                "       \"message\": \"" + "successfully deleted" + "\"\n" +
                "   }\n" +
                "}";
    }
}
