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
        var factory = new GeneratePresignedUrlRequestFactory(bucket, key, now);

        var request = factory.makeDocumentUploadRequest();

        assertThat(request.getBucketName()).isEqualTo(bucket);
        assertThat(request.getKey()).isEqualTo(key);
        assertThat(request.getExpiration()).isEqualTo(now.plusSeconds(1800));
        assertThat(request.getMethod()).isEqualTo(HttpMethod.PUT);
    }

    @Test
    public void makesPresignedUrlRequestWithSixtySecondExpirationForDownloadingDocuments() {
        var bucket = "bucket";
        var key = "key";
        var now = Instant.now();
        var factory = new GeneratePresignedUrlRequestFactory(bucket, key, now);

        var request = factory.makeDocumentDownloadRequest();

        assertThat(request.getBucketName()).isEqualTo(bucket);
        assertThat(request.getKey()).isEqualTo(key);
        assertThat(request.getExpiration()).isEqualTo(now.plusSeconds(60));
        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
    }

    @Test
    public void makesPresignedUrlRequestWithCustomFileNameOverride() {
        var factory = new GeneratePresignedUrlRequestFactory("bucket", "key", Instant.now());
        var fileName = "test.txt";
        var request = factory.withFileNameOverride(fileName).makeDocumentDownloadRequest();

        assertThat(request.getResponseHeaders().getContentDisposition()).isEqualTo("attachment; filename=" + fileName);
    }
}
