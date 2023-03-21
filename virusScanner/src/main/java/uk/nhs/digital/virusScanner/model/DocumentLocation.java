package uk.nhs.digital.virusScanner.model;

import java.net.URI;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DocumentLocation {
    private final URI location;

    public DocumentLocation(String location) {
        this.location = URI.create(location);
    }

    public String getBucketName() {
        return location.getHost();
    }

    public String getPath() {
        return location.getPath().substring(1);
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
        return location.toString();
    }
}
