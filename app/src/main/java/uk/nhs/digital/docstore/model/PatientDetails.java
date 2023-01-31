package uk.nhs.digital.docstore.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class PatientDetails {
    private final List<PatientName> givenName;
    private final PatientName familyName;
    private final BirthDate birthDate;
    private final Postcode postalCode;
    private final NhsNumber nhsNumber;

    public PatientDetails(
            List<PatientName> givenName,
            PatientName familyName,
            BirthDate birthDate,
            Postcode postalCode,
            NhsNumber nhsNumber) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthDate = birthDate;
        this.postalCode = postalCode;
        this.nhsNumber = nhsNumber;
    }

    public List<PatientName> getGivenName() {
        return givenName;
    }

    public PatientName getFamilyName() {
        return familyName;
    }

    public BirthDate getBirthDate() {
        return birthDate;
    }

    public Postcode getPostalCode() {
        return postalCode;
    }

    public NhsNumber getNhsNumber() {
        return nhsNumber;
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return "PatientDetails{"
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
                + '}';
    }
}
