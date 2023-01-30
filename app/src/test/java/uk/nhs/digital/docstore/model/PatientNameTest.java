package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PatientNameTest {
  @Test
  void redactsNameExceptForFirstChar() {
    assertEquals("J***", new PatientName("John").toString());
  }

  @Test
  void shouldNotThrowExceptionWhenRedactingEmptyPatientName() {
    assertDoesNotThrow(() -> new PatientName("").toString());
  }

  @Test
  void shouldThrowNullPointerExceptionWhenRedactingNullName() {
    assertThrows(NullPointerException.class, () -> new PatientName(null).toString());
  }
}
