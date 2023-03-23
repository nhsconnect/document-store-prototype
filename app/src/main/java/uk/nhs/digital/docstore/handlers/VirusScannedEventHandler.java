package uk.nhs.digital.docstore.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import uk.nhs.digital.docstore.events.VirusScannedEvent;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.services.VirusScannedEventService;

public class VirusScannedEventHandler implements RequestHandler<SNSEvent, Void> {

    private final VirusScannedEventService virusScanService;

    public VirusScannedEventHandler(VirusScannedEventService virusScanService) {
        this.virusScanService = virusScanService;
    }

    @Override
    public Void handleRequest(SNSEvent input, Context context) {
        input.getRecords()
                .forEach(
                        (record) -> {
                            var message = record.getSNS().getMessage();
                            try {
                                var virusScannedEvent = VirusScannedEvent.parse(message);
                                String bucketName = virusScannedEvent.getBucketName();
                                String key = virusScannedEvent.getKey();
                                DocumentLocation documentLocation =
                                        new DocumentLocation(
                                                String.format("s3://%s/%s", bucketName, key));
                                virusScanService.updateVirusScanResult(
                                        documentLocation, virusScannedEvent.getResult());
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        });
        return null;
    }
}
