package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BirthDateTest {

  @Test
  void shouldRedactValidBirthDate() {
    assertThat(new BirthDate("2013-12-06").toString()).isEqualTo("****-12-06");
  }

  @Test
  void shouldRedactBirthDateOnlyIncludingYear() {
    assertThat(new BirthDate("2013").toString()).isEqualTo("****");
  }

  @Test
  void shouldRedactBirthDateOnlyIncludingYearAndMonth() {
    assertThat(new BirthDate("2013-10").toString()).isEqualTo("****-10");
  }

  @Test
  void shouldThrowNullPointerExceptionWhenRedactingNullBirthDate() {
    assertThrows(NullPointerException.class, () -> new BirthDate(null).toString());
  }
}
