package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PatientDetails {
    private final List<String> givenName;
    private final String familyName;
    private final String birthdate;
    private final String postalCode;
    private final String nhsNumber;

    public PatientDetails(@JsonProperty("givenName") List<String> givenName,
                          @JsonProperty("familyName") String familyName,
                          @JsonProperty("birthDate") String birthdate,
                          @JsonProperty("postalCode") String postalCode,
                          @JsonProperty("nhsNumber") String nhsNumber) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthdate = birthdate;
        this.postalCode = postalCode;
        this.nhsNumber = nhsNumber;
    }

    public List<String> getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }
}
