package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ApiConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApiConfig.class);
    public static final String AMPLIFY_BASE_URL_ENV_VAR = "AMPLIFY_BASE_URL";

    public String getAmplifyBaseUrl() {
        String url = System.getenv(AMPLIFY_BASE_URL_ENV_VAR);
        if (url == null) {
            logger.warn("Missing required environment variable: " + AMPLIFY_BASE_URL_ENV_VAR);
            return "__unset__AMPLIFY_BASE_URL";
        }
        return url;
    }

    public APIGatewayProxyResponseEvent getApiGatewayResponse(int statusCode, String body, String methods, String location) {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/fhir+json");
        headers.put("Access-Control-Allow-Origin", getAmplifyBaseUrl());
        headers.put("Access-Control-Allow-Methods", methods);

        if (location != null) {
            headers.put("Location", location);
        }

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }
}