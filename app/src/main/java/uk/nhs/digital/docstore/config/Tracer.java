package uk.nhs.digital.docstore.config;
import org.slf4j.MDC;
import uk.nhs.digital.docstore.utils.CommonUtils;

public class Tracer {
    public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

    public static void setMDCContext() {
        clearMDCContext();
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, CommonUtils.generateRandomUUIDString());
    }

    private static void clearMDCContext() {
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
}
