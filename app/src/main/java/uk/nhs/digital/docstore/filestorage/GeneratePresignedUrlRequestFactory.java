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

    public GeneratePresignedUrlRequestFactory(String bucket) {
        this(bucket, Instant.now());
    }

    public GeneratePresignedUrlRequestFactory(String bucket, Instant now) {
        this.now = now;
        this.request = new GeneratePresignedUrlRequest(bucket, null);
    }

    public GeneratePresignedUrlRequestFactory withFileNameOverride(String fileName) {
         request.withResponseHeaders(new ResponseHeaderOverrides().withContentDisposition("attachment; filename=" + fileName));
        return this;
    }

    public GeneratePresignedUrlRequest makeDocumentUploadRequest(String key) {
        return request
                .withKey(key)
                .withExpiration(Date.from(now.plusSeconds(30 * ONE_MINUTE)))
                .withMethod(HttpMethod.PUT);
    }

    public GeneratePresignedUrlRequest makeDocumentDownloadRequest(String key) {
        return request
                .withKey(key)
                .withExpiration(Date.from(now.plusSeconds(ONE_MINUTE)))
                .withMethod(HttpMethod.GET);
    }
}
