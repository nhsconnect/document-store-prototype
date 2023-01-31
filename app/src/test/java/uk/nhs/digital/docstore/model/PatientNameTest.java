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

    @Test
    void isEqualWhenPatientNameValuesAreSame() {

        var patientName1 = new PatientName("John");
        var patientName2 = new PatientName("John");

        assertEquals(patientName1, patientName2);
    }

    @Test
    void isNotEqualWhenPatientNameValuesAreDifferent() {
        var patientName1 = new PatientName("John");
        var patientName2 = new PatientName("Adam");

        assertNotEquals(patientName1, patientName2);
    }
}
