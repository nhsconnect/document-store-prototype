package uk.nhs.digital.virusScanner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

public class VirusScannedEventHandler implements RequestHandler<SNSEvent, Void> {

    @Override
    public Void handleRequest(SNSEvent input, Context context) {
        return null;
    }
}
