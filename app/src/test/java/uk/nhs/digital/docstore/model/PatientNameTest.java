package uk.nhs.digital.docstore.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PatientNameTest {
  @Test
  void redactsNameExceptForFirstChar() {
    assertEquals("J***", new PatientName("John").toString());
  }

  @Test
  void shouldThrowNullPointerExceptionWhenRedactingNullName() {
    assertThrows(NullPointerException.class, () -> new PatientName(null).toString());
  }
}
