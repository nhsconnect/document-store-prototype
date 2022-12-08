package uk.nhs.digital.docstore.patientdetails;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatientDetailsMapperTest {
    private static final String FAMILY_NAME = "Smith";
    private static final String GIVEN_NAME = "Jane";
    private static final String POSTAL_CODE = "LS16AE";
    private static final String BIRTH_DATE = "2010-10-22";

    @Test
    void canDecodeAFullPatientDetailsResponseFromPdsFhir() {
        var nhsNumber = "9000000009";
        var pdsResponse = getPdsResponse(nhsNumber, "complete");

        var patientDetails = new PatientDetailsMapper().fromPatientDetailsResponseBody(pdsResponse);

        assertThat(patientDetails.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(patientDetails.getBirthDate()).isEqualTo(BIRTH_DATE);
        assertThat(patientDetails.getGivenName()).isEqualTo(List.of(GIVEN_NAME));
        assertThat(patientDetails.getFamilyName()).isEqualTo(FAMILY_NAME);
        assertThat(patientDetails.getPostalCode()).isEqualTo(POSTAL_CODE);
    }

    @Test
    void canDecodeARestrictedPatientDetailsResponseFromPdsFhir() {
        var nhsNumber = "9000000025";
        var pdsResponse = getPdsResponse(nhsNumber, "sensitive");

        var patientDetails = new PatientDetailsMapper().fromPatientDetailsResponseBody(pdsResponse);

        assertThat(patientDetails.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(patientDetails.getBirthDate()).isEqualTo(BIRTH_DATE);
        assertThat(patientDetails.getGivenName()).isEqualTo(List.of(GIVEN_NAME));
        assertThat(patientDetails.getFamilyName()).isEqualTo(FAMILY_NAME);
        assertThat(patientDetails.getPostalCode()).isEqualTo(null);
    }

    @Test
    void canDecodeIncompletePatientDetailsResponseFromPdsFhir() {
        var nhsNumber = "9000000033";
        var pdsResponse = getPdsResponse(nhsNumber, "incomplete");

        var patientDetails = new PatientDetailsMapper().fromPatientDetailsResponseBody(pdsResponse);

        assertThat(patientDetails.getNhsNumber()).isEqualTo(nhsNumber);
        assertThat(patientDetails.getBirthDate()).isEqualTo(null);
        assertThat(patientDetails.getGivenName()).isEqualTo(null);
        assertThat(patientDetails.getFamilyName()).isEqualTo(null);
        assertThat(patientDetails.getPostalCode()).isEqualTo(null);
    }

    @Test
    void returnsAPatientDetailsObjectWithNullFieldsIfParsingAnEmptyJson() {
        var patientDetails = new PatientDetailsMapper().fromPatientDetailsResponseBody("{}");

        assertThat(patientDetails.getNhsNumber()).isNull();
        assertThat(patientDetails.getBirthDate()).isNull();
        assertThat(patientDetails.getGivenName()).isNull();
        assertThat(patientDetails.getFamilyName()).isNull();
        assertThat(patientDetails.getPostalCode()).isNull();
    }

    @Test
    void throwsIfNotValidJson() {
        assertThrows(RuntimeException.class, () -> new PatientDetailsMapper().fromPatientDetailsResponseBody("{"));
    }

    private String getPdsResponse(String nhsNumber, String patientStatus) {
        var json = new JSONObject().put("id", nhsNumber);
        if ("complete".equals(patientStatus) || "sensitive".equals(patientStatus)){
            json.put("name", List.of(new PatientDetails.Name(List.of(GIVEN_NAME), FAMILY_NAME)));
            json.put("birthDate", BIRTH_DATE);
            if (!"sensitive".equals(patientStatus)){
                json.put("address", List.of(new PatientDetails.Address(POSTAL_CODE)));
            }
        }
        return  json.toString();


    }
}