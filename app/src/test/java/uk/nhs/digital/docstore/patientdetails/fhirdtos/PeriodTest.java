package uk.nhs.digital.docstore.patientdetails.fhirdtos;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class PeriodTest {
    @Test
    void isCurrentShouldReturnTrueWhenIsACurrentPeriod() {
        var period = new Period(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThat(period.isCurrent()).isTrue();
    }

    @Test
    void isCurrentShouldReturnFalseWhenIsNotACurrentPeriod() {
        var period = new Period(LocalDate.now().minusDays(2), LocalDate.now().minusDays(1));
        assertThat(period.isCurrent()).isFalse();
    }

    @Test
    void isCurrentShouldReturnTrueWhenIsACurrentPeriodAndEndDateIsNull() {
        var period = new Period(LocalDate.now().minusDays(1), null);
        assertThat(period.isCurrent()).isTrue();
    }
}
