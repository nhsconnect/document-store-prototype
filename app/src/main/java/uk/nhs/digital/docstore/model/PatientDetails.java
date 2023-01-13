package uk.nhs.digital.docstore.model;

import java.util.List;

public class PatientDetails {
    private final List<String> givenName;
    private final String familyName;
    private final String birthDate;
    private final String postalCode;
    private final String nhsNumber;

    public PatientDetails(List<String> givenName, String familyName, String birthDate, String postalCode,
                          String nhsNumber) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthDate = birthDate;
        this.postalCode = postalCode;
        this.nhsNumber = nhsNumber;
    }

    public List<String> getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }
}
