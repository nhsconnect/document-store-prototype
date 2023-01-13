package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;

import java.util.Map;

public class Authoriser implements RequestHandler<APIGatewayProxyRequestEvent, IamPolicyResponse> {
    @Override
    public IamPolicyResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        var authoriser = input.getHeaders().get("Authorization");

        var iamPolicy = new IamPolicyResponse();
        iamPolicy.setContext(Map.of("token", authoriser));

        return iamPolicy;
    }
}

