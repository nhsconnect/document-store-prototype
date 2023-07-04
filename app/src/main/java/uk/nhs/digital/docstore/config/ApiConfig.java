package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiConfig.class);
    public static final String AMPLIFY_BASE_URL_ENV_VAR = "AMPLIFY_BASE_URL";

    public String getAmplifyBaseUrl() {
        String workspace = System.getenv("WORKSPACE");
        String url =
                (workspace == null || workspace.isEmpty())
                        ? "https://access-request-fulfilment.patient-deductions.nhs.uk/"
                        : "https://"
                                + workspace
                                + ".access-request-fulfilment.patient-deductions.nhs.uk/";
        if (url == null) {
            LOGGER.warn("Missing required environment variable: " + AMPLIFY_BASE_URL_ENV_VAR);
            return "__unset__AMPLIFY_BASE_URL";
        }
        return url;
    }

    public APIGatewayProxyResponseEvent getApiGatewayResponse(
            int statusCode, String body, String methods, String location) {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/fhir+json");
        headers.put("Access-Control-Allow-Origin", getAmplifyBaseUrl());
        headers.put("Access-Control-Allow-Methods", methods);
        headers.put("Access-Control-Allow-Credentials", "true");
        headers.put("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        if (location != null) {
            headers.put("Location", location);
        }

        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body);
    }
}
