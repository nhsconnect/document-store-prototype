package uk.nhs.digital.virusScanner.model;

import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.nhs.digital.virusScanner.exceptions.IllFormedPatientDetailsException;

public class NhsNumber {
    private static final Pattern PATTERN = Pattern.compile("^[0-9]{10}$");
    private final String value;

    public NhsNumber(String value) throws IllFormedPatientDetailsException {
        if (PATTERN.matcher(value).find()) {
            this.value = value;
        } else {
            throw new IllFormedPatientDetailsException("Invalid NHS Number");
        }
    }

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
        return String.format("%s *** ****", this.value.substring(0, 3));
    }
}
