package uk.nhs.digital.docstore.authoriser.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TracerTest {

    @Test
    void shouldCreateAndAddCorrelationIdToMDC() {
        var context = mock(Context.class);
        var requestId = UUID.randomUUID().toString();

        when(context.getAwsRequestId()).thenReturn(requestId);

        Tracer.setMDCContext(context);

        var mdcCorrelationIdValue = Tracer.getCorrelationId();

        assertThat(mdcCorrelationIdValue).isEqualTo(requestId);
    }
}
