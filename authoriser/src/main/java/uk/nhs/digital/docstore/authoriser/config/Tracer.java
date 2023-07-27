package uk.nhs.digital.docstore.authoriser.config;

import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.MDC;

public class Tracer {
    public static final String CORRELATION_ID_LOG_VAR_NAME = "requestId";

    public static void setMDCContext(Context context) {
        clearMDCContext();
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, context.getAwsRequestId());
    }

    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_LOG_VAR_NAME);
    }

    private static void clearMDCContext() {
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
}
