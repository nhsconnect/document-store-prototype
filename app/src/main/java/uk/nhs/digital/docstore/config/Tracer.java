package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.nhs.digital.docstore.utils.CommonUtils;

import java.util.Map;

public class Tracer {
    private static final Logger logger = LoggerFactory.getLogger(Tracer.class);

    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
    public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

    public static void setMDCContext(APIGatewayProxyRequestEvent requestEvent) {
        clearMDCContext();
        handleCorrelationId(requestEvent.getHeaders());
    }


    private static void handleCorrelationId(Map<String, String> headers) {
        if (headers.containsKey(CORRELATION_ID_HEADER_NAME)) {
            setCorrelationId(headers.get(CORRELATION_ID_HEADER_NAME));
        } else {
            logger.info("The request event has no correlation ID attribute, we'll create and assign one.");
            setCorrelationId(CommonUtils.generateRandomUUIDString());
        }
    }

    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
    }

    private static void clearMDCContext() {
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
}
