package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import uk.nhs.digital.docstore.config.ApiConfig;

public class DeleteDocumentReferenceHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ApiConfig apiConfig;

    public DeleteDocumentReferenceHandler() {
        this(new ApiConfig());
    }

    public DeleteDocumentReferenceHandler(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        String nhsNumber = new NHSNumberSearchParameterForm(input.getQueryStringParameters()).getNhsNumber();
        return apiConfig.getApiGatewayResponse(200, nhsNumber, "DELETE", null);
    }
}
