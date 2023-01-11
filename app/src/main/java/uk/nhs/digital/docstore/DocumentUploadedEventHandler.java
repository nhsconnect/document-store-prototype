package uk.nhs.digital.docstore;

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
import uk.nhs.digital.docstore.services.DocumentReferenceService;

public class DocumentUploadedEventHandler implements RequestHandler<S3Event, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUploadedEventHandler.class);

    private final DocumentReferenceService documentReferenceService;

    @SuppressWarnings("unused")
    public DocumentUploadedEventHandler() {
        this(new DocumentReferenceService(new DocumentMetadataStore(), new SplunkPublisher()));
    }

    public DocumentUploadedEventHandler(DocumentReferenceService documentReferenceService) {
        this.documentReferenceService = documentReferenceService;
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

                documentReferenceService.markDocumentUploaded(location);
            }
        } catch (JsonProcessingException jsonProcessingException) {
            LOGGER.error(jsonProcessingException.getMessage(), jsonProcessingException);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            throw exception;
        }

        return null;
    }
}
