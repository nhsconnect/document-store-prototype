package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

class NhsNumberTest {
  @Test
  public void cannotBuildNhsNumberFromInvalidString() {

    // When
    assertThrows(IllFormedPatientDetailsException.class, () -> new NhsNumber("bananas"));
  }

  @Test
  public void cannotBuildNhsNumberFromTooShortString() {

    // When
    assertThrows(IllFormedPatientDetailsException.class, () -> new NhsNumber("123456789"));
  }

  @Test
  public void cannotBuildNhsNumberFromTooLongString() {

    // When
    assertThrows(IllFormedPatientDetailsException.class, () -> new NhsNumber("12345678901"));
  }

  @Test
  public void shouldRedact() throws IllFormedPatientDetailsException {
    // Given

    // When
    var actual = new NhsNumber("1234567890");

    // Then
    assertThat(actual.toString()).isEqualTo("123 *** ****");
  }
}
