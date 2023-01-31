package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PostcodeTest {
    @Test
    void redactPostcodeAsString() {
        var postcode = new Postcode("AB15 3XY");

        assertThat(postcode.toString()).isEqualTo("AB15 ***");
    }

    @Test
    void doesNotThrowExceptionWhenPostcodeIsEmptyAsString() {
        var postcode = new Postcode("");

        assertDoesNotThrow(postcode::toString);
    }
}
