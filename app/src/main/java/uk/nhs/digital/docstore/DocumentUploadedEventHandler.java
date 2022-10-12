package uk.nhs.digital.docstore;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.config.Tracer;

import java.time.Instant;
import java.util.List;

@SuppressWarnings("unused")
public class DocumentUploadedEventHandler implements RequestHandler<S3Event, Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUploadedEventHandler.class);

    private final DocumentMetadataStore metadataStore = new DocumentMetadataStore();

    @Override
    public Void handleRequest(S3Event event, Context context) {
        Tracer.setMDCContext();
        List<S3EventNotificationRecord> records = event.getRecords();
        LOGGER.info("Marking {} document(s) as uploaded", records.size());

        records.forEach(record -> {
            S3Entity s3 = record.getS3();
            String bucketName = s3.getBucket().getName();
            String objectKey = s3.getObject().getKey();
            String location = String.format("s3://%s/%s", bucketName, objectKey);

            DocumentMetadata metadata = metadataStore.getByLocation(location);
            metadata.setDocumentUploaded(true);
            metadata.setIndexed(Instant.now().toString());

            LOGGER.debug("Updating DocumentReference {} to uploaded", metadata.getId());
            metadataStore.save(metadata);
        });
        return null;
    }
}
