package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

public class PatientDetailsTest {
    @Test
    void shouldReturnPatientDetailsAsString() throws IllFormedPatientDetailsException {
        List<PatientName> givenName = List.of(new PatientName("John"), new PatientName("Max"));
        PatientName familyName = new PatientName("Smith");
        BirthDate birthDate = new BirthDate("1950-03-12");
        Postcode postalCode = new Postcode("A1 BC2");
        NhsNumber nhsNumber = new NhsNumber("9876543210");

        var expectedPatientDetailsString =
                "PatientDetails{"
                        + "givenName='"
                        + givenName
                        + '\''
                        + ", familyName='"
                        + familyName
                        + '\''
                        + ", birthDate='"
                        + birthDate
                        + '\''
                        + ", postalCode="
                        + postalCode
                        + ", nhsNumber='"
                        + nhsNumber
                        + ", superseded="
                        + true
                        + ", restricted="
                        + false
                        + '}';
        var patientDetails =
                new PatientDetails(
                        givenName, familyName, birthDate, postalCode, nhsNumber, true, false);

        assertThat(patientDetails.toString()).isEqualTo(expectedPatientDetailsString);
    }

    @Test
    void shouldReturnPatientDetailsWithNullFieldsAsString()
            throws IllFormedPatientDetailsException {
        NhsNumber nhsNumber = new NhsNumber("9876543210");

        var expectedPatientDetailsString =
                "PatientDetails{"
                        + "givenName='"
                        + null
                        + '\''
                        + ", familyName='"
                        + null
                        + '\''
                        + ", birthDate='"
                        + null
                        + '\''
                        + ", postalCode="
                        + null
                        + ", nhsNumber='"
                        + nhsNumber
                        + ", superseded="
                        + false
                        + ", restricted="
                        + true
                        + '}';
        var patientDetails = new PatientDetails(null, null, null, null, nhsNumber, false, true);

        assertThat(patientDetails.toString()).isEqualTo(expectedPatientDetailsString);
    }

    @Test
    void isEqualWhenPatientDetailsValuesAreSame() throws IllFormedPatientDetailsException {
        List<PatientName> givenName = List.of(new PatientName("John"), new PatientName("Max"));
        PatientName familyName = new PatientName("Smith");
        BirthDate birthDate = new BirthDate("1950-03-12");
        Postcode postalCode = new Postcode("A1 BC2");
        NhsNumber nhsNumber = new NhsNumber("9876543210");

        var patientDetails1 =
                new PatientDetails(
                        givenName, familyName, birthDate, postalCode, nhsNumber, false, false);
        var patientDetails2 =
                new PatientDetails(
                        givenName, familyName, birthDate, postalCode, nhsNumber, false, false);

        assertEquals(patientDetails1, patientDetails2);
    }

    @Test
    void isNotEqualWhenPatientDetailsValuesAreDifferent() throws IllFormedPatientDetailsException {
        List<PatientName> givenName = List.of(new PatientName("John"), new PatientName("Max"));
        PatientName familyName = new PatientName("Smith");
        BirthDate birthDate = new BirthDate("1950-03-12");
        Postcode postalCode1 = new Postcode("A1 BC2");
        NhsNumber nhsNumber = new NhsNumber("9876543210");
        Postcode postalCode2 = new Postcode("UR2 3FG");

        var patientDetails1 =
                new PatientDetails(
                        givenName, familyName, birthDate, postalCode1, nhsNumber, false, false);
        var patientDetails2 =
                new PatientDetails(
                        givenName, familyName, birthDate, postalCode2, nhsNumber, false, false);

        assertNotEquals(patientDetails1, patientDetails2);
    }
}
