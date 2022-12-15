package uk.nhs.digital.docstore.patientdetails;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Address;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Name;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Period;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class PatientDetailsTest {

    @Test
    void canBeInstantiatedFromFhirPatient(){
        var nhsNumber = "9876543210";
        var familyName = "Doe";
        var givenName = List.of("Jane");
        var postalCode = "LS1 6AE";
        var birthDate = "1998-07-11";

        var currentPeriod = new Period(LocalDate.now().minusYears(1), null);
        var currentName = new Name(currentPeriod, "usual", givenName, familyName);
        var currentAddress = new Address(currentPeriod, postalCode, "home");

        var fhirPatient = new Patient(nhsNumber, birthDate, List.of(currentAddress), List.of(currentName));

        var patientDetails = PatientDetails.fromFhirPatient(fhirPatient);

        assertThat(patientDetails.getBirthDate()).isEqualTo(birthDate);
        assertThat(patientDetails.getFamilyName()).isEqualTo(familyName);
        assertThat(patientDetails.getGivenName()).isEqualTo(givenName);
        assertThat(patientDetails.getPostalCode()).isEqualTo(postalCode);
        assertThat(patientDetails.getNhsNumber()).isEqualTo(nhsNumber);
    }
}