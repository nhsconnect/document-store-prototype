package uk.nhs.digital.docstore.model;

import javax.annotation.Nonnull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Postcode {
    @Nonnull private final String value;

    public Postcode(@Nonnull String value) {
        this.value = value;
    }

    @Nonnull
    public String getValue() {
        return value;
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
        return value.substring(0, Math.max(0, value.length() - 3)) + "***";
    }
}
