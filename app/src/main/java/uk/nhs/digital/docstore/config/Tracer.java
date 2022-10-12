package uk.nhs.digital.docstore.config;
import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.MDC;


public class Tracer {
    public static final String CORRELATION_ID_LOG_VAR_NAME = "requestId";

    public static void setMDCContext(Context context) {
        clearMDCContext();
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, context.getAwsRequestId());
    }

    private static void clearMDCContext() {
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
}
