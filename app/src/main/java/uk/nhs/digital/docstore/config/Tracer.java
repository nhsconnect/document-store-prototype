package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class Tracer {
    public static final String TRACE_ID_KEY = "traceId";

    public void setMDCContext(APIGatewayProxyRequestEvent requestEvent) {
        clearMDCContext();
        handleTraceId(requestEvent.getHeaders());
    }

    private void handleTraceId(Map<String, String> headers) {
        if (headers.containsKey(TRACE_ID_KEY)){
            setTraceId(headers.get(TRACE_ID_KEY));
        } else {
            setTraceId(UUID.randomUUID().toString());
        }
    }

    public void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    private void clearMDCContext() {
        MDC.remove(TRACE_ID_KEY);
    }
}
