package uk.nhs.digital.docstore.authoriser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;

public class Authoriser implements RequestHandler<APIGatewayProxyRequestEvent, IamPolicyResponse> {
    @Override
    public IamPolicyResponse handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        System.out.println("hello world *********");
        return null;
    }
}

