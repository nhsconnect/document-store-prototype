package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.utils.CommonUtils;


public class DeleteDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DeleteDocumentReferenceHandler.class);
    private final ApiConfig apiConfig;

    private final CommonUtils utils = new CommonUtils();
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();
    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    public DeleteDocumentReferenceHandler() {
        this(new ApiConfig());
    }

    public DeleteDocumentReferenceHandler(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        logger.info("API Gateway event received - processing starts");

        try {
            var nhsNumber = utils.getNhsNumberFrom(requestEvent.getQueryStringParameters());

            logger.info("Started deleting documents");
            var metadata = metadataStore.findByNhsNumber(nhsNumber);
            if (metadata != null) {
                metadataStore.deleteAndSave(metadata);
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
