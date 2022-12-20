package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.config.Tracer;
import uk.nhs.digital.docstore.data.repository.DocumentMetadataStore;
import uk.nhs.digital.docstore.publishers.SplunkPublisher;
import uk.nhs.digital.docstore.services.DocumentReferenceService;

@SuppressWarnings("unused")
public class DocumentUploadedEventHandler implements RequestHandler<S3Event, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUploadedEventHandler.class);

    private final DocumentReferenceService documentReferenceService = new DocumentReferenceService(
            new DocumentMetadataStore(),
            new SplunkPublisher());

    @Override
    public Void handleRequest(S3Event event, Context context) {
        Tracer.setMDCContext(context);
        var records = event.getRecords();
        LOGGER.info("Marking {} document(s) as uploaded", records.size());

        try {
            for (S3EventNotificationRecord record : records) {
                var s3 = record.getS3();
                var bucketName = s3.getBucket().getName();
                var objectKey = s3.getObject().getKey();
                var location = String.format("s3://%s/%s", bucketName, objectKey);

                documentReferenceService.markDocumentUploaded(location);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
