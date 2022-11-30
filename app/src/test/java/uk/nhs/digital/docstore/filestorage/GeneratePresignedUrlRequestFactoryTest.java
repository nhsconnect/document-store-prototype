package uk.nhs.digital.docstore.filestorage;

import com.amazonaws.HttpMethod;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratePresignedUrlRequestFactoryTest {

    @Test
    public void makesPresignedUrlRequestsWithThirtyMinuteExpirationForUploadingDocuments() {
        var bucket = "bucket";
        var key = "key";
        var now = Instant.now();
        var factory = new GeneratePresignedUrlRequestFactory(bucket, now);
        var thirtyMinutes = 1800;

        var request = factory.makeDocumentUploadRequest(key);

        assertThat(request.getBucketName()).isEqualTo(bucket);
        assertThat(request.getKey()).isEqualTo(key);
        assertThat(request.getExpiration()).isEqualTo(now.plusSeconds(thirtyMinutes));
        assertThat(request.getMethod()).isEqualTo(HttpMethod.PUT);
    }

    @Test
    public void makesPresignedUrlRequestWithSixtySecondExpirationForDownloadingDocuments() {
        var bucket = "bucket";
        var key = "key";
        var now = Instant.now();
        var factory = new GeneratePresignedUrlRequestFactory(bucket, now);
        var ONE_MINUTE = 60;

        var request = factory.makeDocumentDownloadRequest(key);

        assertThat(request.getBucketName()).isEqualTo(bucket);
        assertThat(request.getKey()).isEqualTo(key);
        assertThat(request.getExpiration()).isEqualTo(now.plusSeconds(ONE_MINUTE));
        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void makesPresignedUrlRequestWithCustomFileNameOverride() {
        var factory = new GeneratePresignedUrlRequestFactory("bucket", Instant.now());
        var fileName = "test.txt";
        var request = factory.withFileNameOverride(fileName).makeDocumentDownloadRequest("key");

        assertThat(request.getResponseHeaders().getContentDisposition()).isEqualTo("attachment; filename=" + fileName);
    }
}
