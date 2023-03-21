package uk.nhs.digital.docstore.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirusScannedEventHandler implements RequestHandler<SNSEvent, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirusScannedEventHandler.class);

    @Override
    public Void handleRequest(SNSEvent input, Context context) {
        LOGGER.debug(input.getRecords().get(0).getSNS().getMessage());
        return null;
    }
}
