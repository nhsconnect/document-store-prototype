package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PostcodeTest {
    @Test
    void testToStringRedactsThePostCode() {
        var postcode = new Postcode("AB15 3XY");
        assertThat(postcode.toString()).isEqualTo("AB15 ***");
    }

    @Test
    void testToStringDoesNotThrowWhenPostcodeIsEmpty() {
        var postcode = new Postcode("");
        assertDoesNotThrow(postcode::toString);
    }
}