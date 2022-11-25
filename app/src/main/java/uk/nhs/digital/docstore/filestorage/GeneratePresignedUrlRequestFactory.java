package uk.nhs.digital.docstore.filestorage;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import java.time.Instant;
import java.util.Date;

public class GeneratePresignedUrlRequestFactory {

    private static final int ONE_MINUTE = 60;

    private final Instant now;

    private final GeneratePresignedUrlRequest request;

    public GeneratePresignedUrlRequestFactory(String bucket, String key) {
        this(key, bucket, Instant.now());
    }

    public GeneratePresignedUrlRequestFactory(String bucket, String key, Instant now) {
        this.now = now;
        this.request = new GeneratePresignedUrlRequest(bucket, key);
    }

    public GeneratePresignedUrlRequestFactory withFileNameOverride(String fileName) {
         request.withResponseHeaders(new ResponseHeaderOverrides().withContentDisposition("attachment; filename=" + fileName));
        return this;
    }

    public GeneratePresignedUrlRequest makeDocumentUploadRequest() {
        return request
                .withExpiration(Date.from(now.plusSeconds(30 * ONE_MINUTE)))
                .withMethod(HttpMethod.PUT);
    }

    public GeneratePresignedUrlRequest makeDocumentDownloadRequest() {
        return request
                .withExpiration(Date.from(now.plusSeconds(ONE_MINUTE)))
                .withMethod(HttpMethod.GET);
    }
}
