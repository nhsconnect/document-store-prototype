package uk.nhs.digital.docstore.patientdetails.fhirdtos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.BirthDate;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientName;
import uk.nhs.digital.docstore.model.Postcode;

public class PatientTest {
    @Test
    public void getCurrentUsualNameShouldGetCurrentUsualName() {
        var currentUsualName =
                new Name(
                        new Period(LocalDate.now().minusYears(1), null),
                        "usual",
                        List.of("given name3"),
                        "family name3");

        List<Name> names = new ArrayList<>();
        names.add(
                new Name(
                        new Period(LocalDate.now().minusYears(1), null),
                        "nickname",
                        List.of("given name1"),
                        "family name1"));
        names.add(
                new Name(
                        new Period(LocalDate.now().minusYears(2), LocalDate.now().minusYears(1)),
                        "usual",
                        List.of("given name2"),
                        "family name2"));
        names.add(currentUsualName);

        Patient patient = new Patient("9876543210", null, null, names);

        var result = patient.getCurrentUsualName();
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(currentUsualName);
    }

    @Test
    public void getCurrentUsualNameReturnsEmptyWhenPatientDoesNotHaveNames() {
        Patient patient = new Patient("9876543210", null, null, null);
        assertThat(patient.getCurrentUsualName().isEmpty()).isTrue();
    }

    @Test
    public void getCurrentUsualNameReturnsEmptyWhenPatientHasEmptyNames() {
        Patient patient = new Patient("9876543210", null, null, List.of());
        assertThat(patient.getCurrentUsualName().isEmpty()).isTrue();
    }

    @Test
    public void getCurrentUsualNameReturnsEmptyWhenPatientDoesNotHaveUsualName() {
        var currentNickName =
                new Name(
                        new Period(LocalDate.now().minusYears(1), null),
                        "nickname",
                        List.of("given name3"),
                        "family name3");

        Patient patient = new Patient("9876543210", null, null, List.of(currentNickName));
        assertThat(patient.getCurrentUsualName().isEmpty()).isTrue();
    }

    @Test
    public void getCurrentUsualNameReturnsEmptyWhenPatientDoesNotHaveCurrentName() {
        var oldName =
                new Name(
                        new Period(LocalDate.now().minusYears(2), LocalDate.now().minusYears(1)),
                        "usual",
                        List.of("given name3"),
                        "family name3");

        Patient patient = new Patient("9876543210", null, null, List.of(oldName));
        assertThat(patient.getCurrentUsualName().isEmpty()).isTrue();
    }

    @Test
    public void getCurrentHomeAddressReturnsAMatchingAddress() {
        var currentHomeAddress =
                new Address(new Period(LocalDate.now().minusYears(1), null), "POSTAL_CODE", "home");

        Patient patient = new Patient("9876543210", null, List.of(currentHomeAddress), null);
        assertThat(patient.getCurrentHomeAddress().isPresent()).isTrue();
        assertThat(patient.getCurrentHomeAddress().get()).isEqualTo(currentHomeAddress);
    }

    @Test
    public void getCurrentHomeAddressReturnsEmptyWhenAddressesIsNull() {
        Patient patient = new Patient("9876543210", null, null, null);
        assertThat(patient.getCurrentHomeAddress().isEmpty()).isTrue();
    }

    @Test
    public void getCurrentHomeAddressReturnsEmptyWhenAddressesIsEmpty() {
        Patient patient = new Patient("9876543210", null, List.of(), null);
        assertThat(patient.getCurrentHomeAddress().isEmpty()).isTrue();
    }

    @Test
    public void getCurrentHomeAddressReturnsEmptyWhenNoHomeAddress() {
        var currentBillingAddress =
                new Address(
                        new Period(LocalDate.now().minusYears(1), null), "POSTAL_CODE", "billing");

        Patient patient = new Patient("9876543210", null, List.of(currentBillingAddress), null);
        assertThat(patient.getCurrentHomeAddress().isEmpty()).isTrue();
    }

    @Test
    public void getCurrentHomeAddressReturnsEmptyWhenNoCurrentAddress() {
        var currentBillingAddress =
                new Address(
                        new Period(LocalDate.now().minusYears(2), LocalDate.now().minusYears(1)),
                        "POSTAL_CODE",
                        "home");

        Patient patient = new Patient("9876543210", null, List.of(currentBillingAddress), null);
        assertThat(patient.getCurrentHomeAddress().isEmpty()).isTrue();
    }

    @Test
    void canCreateAPatientFromPdsFhirResponse() {
        var familyName = "Smith";
        var givenName = List.of("Jane");
        var postalCode = "LS16AE";
        var birthDate = "2010-10-22";
        var nhsNumber = "9000000009";

        var jsonPeriod =
                new JSONObject()
                        .put("start", LocalDate.now().minusYears(1).toString())
                        .put("end", JSONObject.NULL);

        var jsonName =
                new JSONObject()
                        .put("period", jsonPeriod)
                        .put("use", "usual")
                        .put("given", givenName)
                        .put("family", familyName);

        var jsonAddress =
                new JSONObject()
                        .put("period", jsonPeriod)
                        .put("use", "home")
                        .put("postalCode", postalCode);

        var pdsResponse =
                new JSONObject()
                        .put("id", nhsNumber)
                        .put("name", List.of(jsonName))
                        .put("birthDate", birthDate)
                        .put("address", List.of(jsonAddress))
                        .toString();

        var patientDetails = Patient.parseFromJson(pdsResponse);

        assertThat(patientDetails.getId()).isEqualTo(nhsNumber);
        assertThat(patientDetails.getBirthDate()).isEqualTo(birthDate);
        assertThat(patientDetails.getCurrentUsualName().isPresent()).isTrue();
        assertThat(patientDetails.getCurrentUsualName().get().getGiven()).isEqualTo(givenName);
        assertThat(patientDetails.getCurrentUsualName().get().getFamily()).isEqualTo(familyName);
        assertThat(patientDetails.getCurrentHomeAddress().isPresent()).isTrue();
        assertThat(patientDetails.getCurrentHomeAddress().get().getPostalCode())
                .isEqualTo(postalCode);
    }

    @Test
    void canCreatePatientDetailsFromFhirPatient() throws IllFormedPatientDetailsException {
        var nhsNumber = new NhsNumber("9876543210");
        var familyName = "Doe";
        var givenName = List.of("Jane", "John");
        var givenPatientName = List.of(new PatientName("Jane"), new PatientName("John"));
        var postalCode = "LS1 6AE";
        var birthDate = "1998-07-11";
        var currentPeriod = new Period(LocalDate.now().minusYears(1), null);
        var currentName = new Name(currentPeriod, "usual", givenName, familyName);
        var currentAddress = new Address(currentPeriod, postalCode, "home");
        var fhirPatient =
                new Patient(
                        nhsNumber.getValue(),
                        birthDate,
                        List.of(currentAddress),
                        List.of(currentName));

        var patientDetails = fhirPatient.parse(nhsNumber);

        assertTrue(patientDetails.getBirthDate().isPresent());
        assertThat(patientDetails.getBirthDate().get()).isEqualTo(new BirthDate(birthDate));
        assertTrue(patientDetails.getFamilyName().isPresent());
        assertThat(patientDetails.getFamilyName().get()).isEqualTo(new PatientName(familyName));
        assertTrue(patientDetails.getGivenName().isPresent());
        assertThat(patientDetails.getGivenName().get()).isEqualTo(givenPatientName);
        assertTrue(patientDetails.getPostalCode().isPresent());
        assertThat(patientDetails.getPostalCode().get()).isEqualTo(new Postcode(postalCode));
        assertThat(patientDetails.getNhsNumber()).isEqualTo(nhsNumber);
        assertFalse(patientDetails.isSuperseded());
    }

    @Test
    void canCreatePatientDetailsFromSupersededFhirPatient()
            throws IllFormedPatientDetailsException {
        var requestedNhsNumber = new NhsNumber("9000000017");
        var receivedNhsNumber = new NhsNumber("9000000018");
        var familyName = "Doe";
        var givenName = List.of("Jane", "John");
        var givenPatientName = List.of(new PatientName("Jane"), new PatientName("John"));
        var postalCode = "LS1 6AE";
        var birthDate = "1998-07-11";
        var currentPeriod = new Period(LocalDate.now().minusYears(1), null);
        var currentName = new Name(currentPeriod, "usual", givenName, familyName);
        var currentAddress = new Address(currentPeriod, postalCode, "home");
        var fhirPatient =
                new Patient(
                        receivedNhsNumber.getValue(),
                        birthDate,
                        List.of(currentAddress),
                        List.of(currentName));

        var patientDetails = fhirPatient.parse(requestedNhsNumber);

        assertTrue(patientDetails.getBirthDate().isPresent());
        assertThat(patientDetails.getBirthDate().get()).isEqualTo(new BirthDate(birthDate));
        assertTrue(patientDetails.getFamilyName().isPresent());
        assertThat(patientDetails.getFamilyName().get()).isEqualTo(new PatientName(familyName));
        assertTrue(patientDetails.getGivenName().isPresent());
        assertThat(patientDetails.getGivenName().get()).isEqualTo(givenPatientName);
        assertTrue(patientDetails.getPostalCode().isPresent());
        assertThat(patientDetails.getPostalCode().get()).isEqualTo(new Postcode(postalCode));
        assertThat(patientDetails.getNhsNumber()).isEqualTo(receivedNhsNumber);
        assertThat(patientDetails.getNhsNumber()).isNotEqualTo(requestedNhsNumber);
        assertTrue(patientDetails.isSuperseded());
    }
}
