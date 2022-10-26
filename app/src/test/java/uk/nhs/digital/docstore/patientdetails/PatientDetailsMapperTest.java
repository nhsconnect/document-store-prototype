package uk.nhs.digital.docstore.patientdetails;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PatientDetailsMapperTest {

    @Test
    void canDecodeAFullPatientDetailsResponseFromPdsAdaptor() {
        var patientDetailsJson = JsonMapper.toJson(Map.of("nhsNumber", "9000000009",
                "givenName", List.of("Foo", "Bar"),
                "familyName", "Baz",
                "postalCode", "LS1 4DX",
                "birthdate", "1980-10-14"));

        var patientDetails = new PatientDetailsMapper().fromPatientDetailsResponseBody(patientDetailsJson);

        assertThat(patientDetails.getNhsNumber()).isEqualTo("9000000009");
        assertThat(patientDetails.getBirthdate()).isEqualTo("1980-10-14");
        assertThat(patientDetails.getGivenName()).isEqualTo(List.of("Foo", "Bar"));
        assertThat(patientDetails.getFamilyName()).isEqualTo("Baz");
        assertThat(patientDetails.getPostalCode()).isEqualTo("LS1 4DX");
    }

    @Test
    void returnsAPatientDetailsObjectWithNullFieldsIfParsingAnEmptyJson() {
        var patientDetails = new PatientDetailsMapper().fromPatientDetailsResponseBody("{}");

        assertThat(patientDetails.getNhsNumber()).isNull();
        assertThat(patientDetails.getBirthdate()).isNull();
        assertThat(patientDetails.getGivenName()).isNull();
        assertThat(patientDetails.getFamilyName()).isNull();
        assertThat(patientDetails.getPostalCode()).isNull();
    }

    @Test
    void throwsIfNotValidJson() {
        assertThrows(RuntimeException.class, () -> new PatientDetailsMapper().fromPatientDetailsResponseBody("{"));
    }

}