package uk.nhs.digital.docstore.patientdetails;

import java.util.List;

public class PatientDetails {
    private final List<String> givenName;
    private final String familyName;
    private final String birthdate;
    private final String postalCode;
    private final String nhsNumber;

    public PatientDetails(List<String> givenName, String familyName, String birthdate, String postalCode, String nhsNumber) {
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
