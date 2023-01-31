package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NhsNumberTest {
    @Test
    public void cannotBuildNhsNumberFromInvalidString() {
        assertThrows(IllFormedPatientDetailsException.class, () -> new NhsNumber("bananas"));
    }

    @Test
    public void cannotBuildNhsNumberFromTooShortString() {
        assertThrows(IllFormedPatientDetailsException.class, () -> new NhsNumber("123456789"));
    }

    @Test
    public void cannotBuildNhsNumberFromTooLongString() {
        assertThrows(IllFormedPatientDetailsException.class, () -> new NhsNumber("12345678901"));
    }

    @Test
    public void redactsNhsNumberAsString() throws IllFormedPatientDetailsException {
        var actual = new NhsNumber("1234567890");

        assertThat(actual.toString()).isEqualTo("123 *** ****");
    }

    @Test
    void isEqualWhenNhsNumberValuesAreSame() throws IllFormedPatientDetailsException {

        var nhsNumber1 = new NhsNumber("9876543210");
        var nhsNumber2 = new NhsNumber("9876543210");

        assertEquals(nhsNumber1, nhsNumber2);
    }

    @Test
    void isNotEqualWhenNhsNumberValuesAreDifferent() throws IllFormedPatientDetailsException {
        var nhsNumber1 = new NhsNumber("9876543210");
        var nhsNumber2 = new NhsNumber("9123456780");

        assertNotEquals(nhsNumber1, nhsNumber2);
    }
}
