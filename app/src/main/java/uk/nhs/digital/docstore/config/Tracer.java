package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.nhs.digital.docstore.CreateDocumentReferenceHandler;
import uk.nhs.digital.docstore.utils.CommonUtils;

import java.util.Map;
import java.util.UUID;

public class Tracer {
    private static final Logger logger = LoggerFactory.getLogger(Tracer.class);

    public static final String TRACE_ID_KEY = "traceId";

    public static void setMDCContext(APIGatewayProxyRequestEvent requestEvent) {
        clearMDCContext();
        handleTraceId(requestEvent.getHeaders());
    }

    private static void handleTraceId(Map<String, String> headers) {
        if (headers.containsKey(TRACE_ID_KEY)) {
            setTraceId(headers.get(TRACE_ID_KEY));
        } else {
            logger.info("The request event has no trace ID attribute, we'll create and assign one.");
            setTraceId(CommonUtils.generateRandomUUIDString());
        }
    }

    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    private static void clearMDCContext() {
        MDC.remove(TRACE_ID_KEY);
    }
}
