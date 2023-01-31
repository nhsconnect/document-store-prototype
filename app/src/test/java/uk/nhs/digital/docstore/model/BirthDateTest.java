package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

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
}
