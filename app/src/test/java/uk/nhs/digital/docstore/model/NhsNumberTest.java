package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NhsNumberTest {
    @Test
    public void cannotBuildNhsNumberFromInvalidString() {

        // When
        assertThrows(IllFormedPatentDetailsException.class, () -> new NhsNumber("bananas"));
    }

    @Test
    public void cannotBuildNhsNumberFromTooShortString() {

        // When
        assertThrows(IllFormedPatentDetailsException.class, () -> new NhsNumber("123456789"));
    }

    @Test
    public void cannotBuildNhsNumberFromTooLongString() {

        // When
        assertThrows(IllFormedPatentDetailsException.class, () -> new NhsNumber("12345678901"));
    }

    @Test
    public void shouldRedact() throws IllFormedPatentDetailsException {
        // Given

        // When
        var actual = new NhsNumber("1234567890");

        // Then
        assertThat(actual.toString()).isEqualTo("123 *** ****");
    }
}