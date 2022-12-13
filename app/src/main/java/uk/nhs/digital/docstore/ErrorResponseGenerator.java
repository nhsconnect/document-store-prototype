package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.config.ApiConfig;
import uk.nhs.digital.docstore.exceptions.OperationOutcomeIssuable;

public class ErrorResponseGenerator {
    private static final Logger logger
            = LoggerFactory.getLogger(ErrorResponseGenerator.class);

    private final ApiConfig apiConfig;

    public ErrorResponseGenerator() {
        this(new ApiConfig());
    }

    public ErrorResponseGenerator(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public APIGatewayProxyResponseEvent errorResponse(Exception e) {
        int statusCode;
        String body;

        if (e instanceof OperationOutcomeIssuable) {
            statusCode = 400;
            body = getErrorResponseBody(e.getMessage());
        }  else {
            statusCode = 500;
            body = getErrorResponseBody("Internal server error");
        }

        logger.error(e.getMessage(), e);

        return apiConfig.getApiGatewayResponse(statusCode, body, "GET, OPTIONS, POST", null);
    }

    public APIGatewayProxyResponseEvent outOfMemoryResponse(OutOfMemoryError e) {
        logger.error(e.getMessage(), e);
        var body = "File too large: " +  e.getMessage();
        return apiConfig.getApiGatewayResponse(507, body, "GET, OPTIONS, POST", null);
    }

    private String getErrorResponseBody(String message) {
        return "{\n" +
                "   \"errorMessage\": \""+ message +"\"\n" +
                "}";
    }
}
