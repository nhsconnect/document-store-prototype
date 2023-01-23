package uk.nhs.digital.docstore.model;

import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;

import java.util.regex.Pattern;

public class NhsNumber {
    private static final Pattern PATTERN = Pattern.compile("^[0-9]{10}$");
    private final String value;

    public NhsNumber(String value) throws IllFormedPatentDetailsException {
        if (PATTERN.matcher(value).find()) {
            this.value = value;
        } else {
            throw new IllFormedPatentDetailsException("Invalid NHS Number");
        }
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NhsNumber nhsNumber = (NhsNumber) o;
        return nhsNumber.value.equals(this.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s *** ****", this.value.substring(0, 3));
    }
}