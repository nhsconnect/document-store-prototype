package uk.nhs.digital.docstore.config;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.nhs.digital.docstore.config.Tracer.CORRELATION_ID_LOG_VAR_NAME;

@ExtendWith(MockitoExtension.class)
class TracerTest {

    @Test
    void shouldCreateAndAddCorrelationIdToMDC() {
        var context = mock(Context.class);
        var requestId = UUID.randomUUID().toString();

        when(context.getAwsRequestId()).thenReturn(requestId);

        Tracer.setMDCContext(context);

        var mdcCorrelationIdValue = MDC.get(CORRELATION_ID_LOG_VAR_NAME);

        assertThat(mdcCorrelationIdValue).isEqualTo(requestId);
    }

}