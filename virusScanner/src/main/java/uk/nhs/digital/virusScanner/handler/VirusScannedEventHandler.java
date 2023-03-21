package uk.nhs.digital.virusScanner.handler;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirusScannedEventHandler implements RequestHandler<SNSEvent, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VirusScannedEventHandler.class);

    final DynamoDB dynamoDB;

    public VirusScannedEventHandler(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public Void handleRequest(SNSEvent input, Context context) {
        LOGGER.debug(input.getRecords().get(0).getSNS().getMessage());
        return null;
    }
}
