package uk.nhs.digital.docstore.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PatientNameTest {
    @Test
    void redactsNameExceptForFirstChar() {
        assertEquals("J***", new PatientName("John").toString());
    }

    @Test
    void shouldNotThrowExceptionWhenRedactingEmptyPatientName() {
        assertDoesNotThrow(() -> new PatientName("").toString());
    }
}
