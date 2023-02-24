package uk.nhs.digital.docstore.model;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PatientDetails {
    @Nullable private final List<PatientName> givenName;
    @Nullable private final PatientName familyName;
    @Nullable private final BirthDate birthDate;
    @Nullable private final Postcode postalCode;
    @Nonnull private final NhsNumber nhsNumber;
    private final boolean superseded;

    public PatientDetails(
            @Nullable List<PatientName> givenName,
            @Nullable PatientName familyName,
            @Nullable BirthDate birthDate,
            @Nullable Postcode postalCode,
            @Nonnull NhsNumber nhsNumber,
            boolean superseded) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthDate = birthDate;
        this.postalCode = postalCode;
        this.nhsNumber = nhsNumber;
        this.superseded = superseded;
    }

    public Optional<List<PatientName>> getGivenName() {
        return Optional.ofNullable(givenName);
    }

    public Optional<PatientName> getFamilyName() {
        return Optional.ofNullable(familyName);
    }

    public Optional<BirthDate> getBirthDate() {
        return Optional.ofNullable(birthDate);
    }

    public Optional<Postcode> getPostalCode() {
        return Optional.ofNullable(postalCode);
    }

    @Nonnull
    public NhsNumber getNhsNumber() {
        return nhsNumber;
    }

    public boolean isSuperseded() {
        return superseded;
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
                + ", superseded="
                + superseded
                + '}';
    }
}
