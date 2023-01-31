package uk.nhs.digital.docstore.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PatientNameTest {
    @Test
    void redactPatientNameAsString() {
        assertEquals("J***", new PatientName("John").toString());
    }

    @Test
    void doesNotThrowExceptionWhenPatientNameIsEmptyAsString() {
        assertDoesNotThrow(() -> new PatientName("").toString());
    }
}
