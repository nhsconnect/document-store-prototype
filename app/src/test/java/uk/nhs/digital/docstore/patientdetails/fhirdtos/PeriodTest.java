package uk.nhs.digital.docstore.patientdetails.fhirdtos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

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
