package uk.nhs.digital.docstore.model;

import java.util.Arrays;
import javax.annotation.Nonnull;
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
        var fileNameAndTypes = value.split("\\.");
        var redactedFileName =
                fileNameAndTypes[0].charAt(0)
                        + "***"
                        + fileNameAndTypes[0].charAt(fileNameAndTypes[0].length() - 1);
        var fileTypes = fileNameAndTypes.length > 1 ? extractFileTypes(fileNameAndTypes) : "";

        return redactedFileName + fileTypes;
    }

    private String extractFileTypes(String[] fileNameAndTypes) {
        var fileTypes = Arrays.asList(fileNameAndTypes).subList(1, fileNameAndTypes.length);
        var stringBuilder = new StringBuilder();

        for (String fileType : fileTypes) {
            stringBuilder.append(".").append(fileType);
        }

        return stringBuilder.toString();
    }
}
