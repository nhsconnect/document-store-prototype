package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;

import java.time.Instant;

public class DeleteDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ApiConfig apiConfig;
    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();

    public DeleteDocumentReferenceHandler() {
        this(new ApiConfig());
    }

    public DeleteDocumentReferenceHandler(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String nhsNumber = new NHSNumberSearchParameterForm(input.getQueryStringParameters()).getNhsNumber();
        var metadata = metadataStore.findByNhsNumber(nhsNumber);
        if(metadata != null){
            for (var item : metadata) {
                item.setDeleted(Instant.now().toString());
                metadataStore.save(item);
            }
        }
        return apiConfig.getApiGatewayResponse(200, nhsNumber, "DELETE", null);
    }
}
