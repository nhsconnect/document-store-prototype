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

    @Test
    void isEqualWhenPostCodeValuesAreSame() {

        var postCode1 = new Postcode("AB1 2CD");
        var postCode2 = new Postcode("AB1 2CD");

        assertEquals(postCode1, postCode2);
    }

    @Test
    void isNotEqualWhenPostCodeValuesAreDifferent() {
        var postCode1 = new Postcode("AB1 2CD");
        var postCode2 = new Postcode("CD4 5EF");

        assertNotEquals(postCode1, postCode2);
    }
}
