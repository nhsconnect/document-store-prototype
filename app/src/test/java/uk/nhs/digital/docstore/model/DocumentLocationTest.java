package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class DocumentLocationTest {
    @Test
    void documentLocationsWithTheSameFieldValuesAreEqual() {
        var location = "some/location";
        var documentLocation1 = new DocumentLocation(location);
        var documentLocation2 = new DocumentLocation(location);

        assertEquals(documentLocation1, documentLocation2);
    }

    @Test
    void documentLocationsWithDifferentFieldValuesAreNotEqual() {
        var documentLocation1 = new DocumentLocation("some/location");
        var documentLocation2 = new DocumentLocation("other/location");

        assertNotEquals(documentLocation1, documentLocation2);
    }

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
