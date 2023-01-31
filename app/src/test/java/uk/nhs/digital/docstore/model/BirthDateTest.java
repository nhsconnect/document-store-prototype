package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BirthDateTest {

    @Test
    void redactBirthDateAsStringWithYearMonthDay() {
        assertThat(new BirthDate("2013-12-06").toString()).isEqualTo("****-12-06");
    }

    @Test
    void redactBirthDateAsStringWithYear() {
        assertThat(new BirthDate("2013").toString()).isEqualTo("****");
    }

    @Test
    void redactBirthDateAsStringWithYearMonth() {
        assertThat(new BirthDate("2013-10").toString()).isEqualTo("****-10");
    }

    @Test
    void doesNotThrowExceptionWhenBirthDateIsAnEmptyString() {
        assertDoesNotThrow(() -> new BirthDate("").toString());
    }

    @Test
    void isEqualWhenValuesAreSame() {
        var birthDate1 = new BirthDate("2021-12-01");
        var birthDate2 = new BirthDate("2021-12-01");

        assertEquals(birthDate1, birthDate2);
    }

    @Test
    void isNotEqualWhenValuesAreDifferent() {
        var birthDate1 = new BirthDate("2021-12-01");
        var birthDate2 = new BirthDate("2020-01-11");

        assertNotEquals(birthDate1, birthDate2);
    }
}
