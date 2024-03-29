package uk.nhs.digital.docstore.model;

import javax.annotation.Nonnull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FileName {
    @Nonnull private final String value;

    public FileName(@Nonnull String value) {
        this.value = value;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    public String getBaseName() {
        return FilenameUtils.getBaseName(value);
    }

    public String getExtension() {
        var extension = FilenameUtils.getExtension(value);
        return extension.length() > 0 ? "." + extension : "";
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
        var baseFileName = getBaseName();
        var redactedFileName =
                baseFileName.charAt(0) + "***" + baseFileName.charAt(baseFileName.length() - 1);

        return redactedFileName + getExtension();
    }
}
