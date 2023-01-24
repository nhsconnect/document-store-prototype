package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.data.serialiser.DocumentMetadataSerialiser;
import uk.nhs.digital.docstore.services.DocumentDeletionService;


public class DeleteDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDocumentReferenceHandler.class);

    private final ApiConfig apiConfig;
    private final DocumentDeletionService documentDeletionService;

    private final ErrorResponseGenerator errorResponseGenerator = new ErrorResponseGenerator();

    @SuppressWarnings("unused")
    public DeleteDocumentReferenceHandler() {
        this(
                new ApiConfig(),
                new DocumentDeletionService(
                        new SplunkPublisher(),
                        new DocumentStore(System.getenv("DOCUMENT_STORE_BUCKET_NAME")),
                        new DocumentMetadataStore(),
                        new DocumentMetadataSerialiser()
                )
        );
    }

    public DeleteDocumentReferenceHandler(ApiConfig apiConfig, DocumentDeletionService documentDeletionService) {
        this.apiConfig = apiConfig;
        this.documentDeletionService = documentDeletionService;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        Tracer.setMDCContext(context);

        LOGGER.debug("API Gateway event received - processing starts");

        try {
            var nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(requestEvent.getQueryStringParameters());
            var nhsNumber = nhsNumberSearchParameterForm.getNhsNumber();
            documentDeletionService.deleteAllDocumentsForPatient(nhsNumber);

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
