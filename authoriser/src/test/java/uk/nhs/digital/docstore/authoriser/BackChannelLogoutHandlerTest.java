package uk.nhs.digital.docstore.authoriser;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BackChannelLogoutHandlerTest {
    @Test
    public void destroysTheSessionWhenTheSessionIdIsValid() {
        var handler = new BackChannelLogoutHandler();
        handler.handleRequest(new APIGatewayProxyRequestEvent(), Mockito.mock(Context.class));
    }
}
