package uk.nhs.digital.docstore.model;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PatientName {
    @Nonnull private final String value;

    public PatientName(@Nonnull String value) {
        this.value = value;
    }

    @Nonnull
    public String getValue() {
        return this.value;
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
        return (value.length() > 0 ? String.valueOf(value.charAt(0)) : "") + "***";
    }
}
