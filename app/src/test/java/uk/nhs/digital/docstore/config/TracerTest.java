package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static uk.nhs.digital.docstore.config.Tracer.TRACE_ID_KEY;

class TracerTest {
    public static final String TRACE_ID_VALUE = "some-trace-id";
    private static Tracer tracer;
    private APIGatewayProxyRequestEvent requestEvent;

    @BeforeAll
    static void setUp() {
        tracer = new Tracer();
    }

    @BeforeEach
    public void testSetUp() {requestEvent = spy(new APIGatewayProxyRequestEvent());}

    @Test
    public void shouldAddTraceIdToMDCWhenItIsPresentInMessage() {
        var headers = Map.of(TRACE_ID_KEY, TRACE_ID_VALUE);

        requestEvent.setHeaders(headers);

        tracer.setMDCContext(requestEvent);

        var mdcTraceIdValue = MDC.get(TRACE_ID_KEY);

        assertThat(mdcTraceIdValue).isEqualTo(TRACE_ID_VALUE);
    }

    @Test
    void shouldCreateAndAddTraceIdToMDCWhenItIsNotPresentInMessage() {
        var emptyHeaders = Map.of("", "");
        requestEvent.setHeaders(emptyHeaders);

        tracer.setMDCContext(requestEvent);

        var mdcTraceIdValue = MDC.get(TRACE_ID_KEY);

        assertThat(mdcTraceIdValue).isNotNull();
        assertThat(UUID.fromString(mdcTraceIdValue)).isNotNull();
    }

}