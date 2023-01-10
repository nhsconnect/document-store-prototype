package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.utils.CommonUtils;
import uk.nhs.digital.docstore.utils.DeleteMarkerUtil;


public class DeleteDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DeleteDocumentReferenceHandler.class);
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

    public DeleteDocumentReferenceHandler(ApiConfig apiConfig,  AmazonS3 s3client) {

        this.apiConfig = apiConfig;
        this.s3client = s3client;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        logger.info("API Gateway event received - processing starts");

        try {
            var nhsNumber = utils.getNhsNumberFrom(requestEvent.getQueryStringParameters());

            logger.info("Started deleting documents from dynamodb");
            var metadata = metadataStore.findByNhsNumber(nhsNumber);
            if (metadata != null) {
                metadataStore.deleteAndSave(metadata);

                logger.info("Started deleting documents from s3");
                metadata.forEach(documentMetadata -> {
                    var bucketName = documentMetadata.getLocation().split("//")[1].split("/")[0];
                    var objectPrefix =  documentMetadata.getLocation().split("//")[1].split("/")[1];
                    logger.info("Showing the bucketName "+bucketName + " object prefix " + objectPrefix);
                    if (!DeleteMarkerUtil.markDocumentAsDelete(s3client,bucketName, objectPrefix)) {
                        logger.error("It is not possible to delete document from s3");
                    }
                });
            }

            logger.info("Processing finished - about to return the response");
            var body = getJsonBody("successfully deleted");
            return apiConfig.getApiGatewayResponse(200, body, "DELETE", null);
        } catch (Exception e) {
            return errorResponseGenerator.errorResponse(e);
        }
    }

    private String getJsonBody(String successfullyDeleted) {
        return "{\n" +
                "   \"result\": {\n" +
                "       \"message\": \"" + successfullyDeleted + "\"\n" +
                "   }\n" +
                "}";
    }
}
