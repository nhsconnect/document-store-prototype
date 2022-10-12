package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static uk.nhs.digital.docstore.config.Tracer.CORRELATION_ID_LOG_VAR_NAME;

class TracerTest {
    private static Tracer tracer;
    private APIGatewayProxyRequestEvent requestEvent;

    @BeforeAll
    static void setUp() {
        tracer = new Tracer();
    }

    @BeforeEach
    public void testSetUp() {requestEvent = spy(new APIGatewayProxyRequestEvent());}

    @Test
    void shouldCreateAndAddCorrelationIdToMDC() {

        tracer.setMDCContext();

        var mdcCorrelationIdValue = MDC.get(CORRELATION_ID_LOG_VAR_NAME);

        assertThat(mdcCorrelationIdValue).isNotNull();
        assertThat(UUID.fromString(mdcCorrelationIdValue)).isNotNull();
    }

}