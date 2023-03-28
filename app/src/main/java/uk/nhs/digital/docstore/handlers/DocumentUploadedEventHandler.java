package uk.nhs.digital.docstore.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.DocumentLocation;
import uk.nhs.digital.docstore.services.VirusScannedEventService;

public class DocumentUploadedEventHandler implements RequestHandler<S3Event, Void> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(DocumentUploadedEventHandler.class);
    private static final String SCAN_RESULT = "Clean";

    private final VirusScannedEventService virusScannedEventService;

    @SuppressWarnings("unused")
    public DocumentUploadedEventHandler() {
        this(
                new VirusScannedEventService(
                        new DocumentMetadataStore(),
                        new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL"))));
    }

    public DocumentUploadedEventHandler(VirusScannedEventService virusScannedEventService) {
        this.virusScannedEventService = virusScannedEventService;
    }

    @Override
    public Void handleRequest(S3Event s3Event, Context context) {
        Tracer.setMDCContext(context);

        var records = s3Event.getRecords();
        LOGGER.info("Marking {} document(s) as uploaded", records.size());

        try {
            for (S3EventNotificationRecord record : records) {
                var s3 = record.getS3();
                var bucketName = s3.getBucket().getName();
                var objectKey = s3.getObject().getKey();
                var location = String.format("s3://%s/%s", bucketName, objectKey);

                virusScannedEventService.updateVirusScanResult(
                        new DocumentLocation(location), SCAN_RESULT);
            }
        } catch (JsonProcessingException | IllFormedPatientDetailsException exception) {
            LOGGER.error(exception.getMessage(), exception);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            throw exception;
        }

        return null;
    }
}
