package uk.nhs.digital.docstore.filestorage;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

public class GeneratePresignedUrlRequestFactory {

  private static final int ONE_MINUTE = 60;

  private final Clock clock;

  private final GeneratePresignedUrlRequest request;

  public GeneratePresignedUrlRequestFactory(String bucket) {
    this(bucket, Clock.systemUTC());
  }

  public GeneratePresignedUrlRequestFactory(String bucket, Clock clock) {
    this.clock = clock;
    this.request = new GeneratePresignedUrlRequest(bucket, null);
  }

  public GeneratePresignedUrlRequestFactory withFileNameOverride(String fileName) {
    request.withResponseHeaders(
        new ResponseHeaderOverrides().withContentDisposition("attachment; filename=" + fileName));
    return this;
  }

  public GeneratePresignedUrlRequest makeDocumentUploadRequest(String key) {
    return request
        .withKey(key)
        .withExpiration(Date.from(Instant.now(clock).plusSeconds(30 * ONE_MINUTE)))
        .withMethod(HttpMethod.PUT);
  }

  public GeneratePresignedUrlRequest makeDocumentDownloadRequest(String key) {
    return request
        .withKey(key)
        .withExpiration(Date.from(Instant.now(clock).plusSeconds(ONE_MINUTE)))
        .withMethod(HttpMethod.GET);
  }
}
