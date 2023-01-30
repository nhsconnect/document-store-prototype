package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentLocationTest {

    @Test
    void getPathFromLocation() {
        var bucketName = "some-bucket-name";
        var path = "some-path";
        var location = String.format("s3://%s/%s", bucketName, path);
        var docLocation = new DocumentLocation(location);
        assertThat(docLocation.getPath()).isEqualTo(path);
    }

    @Test
    void getBucketNameFromLocation() {
        var bucketName = "another-bucket-name";
        var path = "some-other-path";
        var location = String.format("s3://%s/%s", bucketName, path);
        var docLocation = new DocumentLocation(location);
        assertThat(docLocation.getBucketName()).isEqualTo(bucketName);
    }
}
