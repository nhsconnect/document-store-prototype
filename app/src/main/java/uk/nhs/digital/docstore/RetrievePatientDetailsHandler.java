package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class RetrievePatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200);
    }
}
