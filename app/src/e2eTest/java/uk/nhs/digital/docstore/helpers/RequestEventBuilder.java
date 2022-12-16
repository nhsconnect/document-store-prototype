package uk.nhs.digital.docstore.helpers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.HashMap;

public class RequestEventBuilder {
    private final HashMap<String, String> parameters = new HashMap<>();

    public RequestEventBuilder addQueryParameter(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    public APIGatewayProxyRequestEvent build() {
        return new APIGatewayProxyRequestEvent().withQueryStringParameters(parameters);
    }
}
