package uk.nhs.digital.docstore.model;

public class Postcode {
    private final String value;

    public Postcode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Postcode postcode = (Postcode) o;
        return postcode.value.equals(this.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.substring(0, Math.max(0, value.length() - 3)) + "***";
    }
}
