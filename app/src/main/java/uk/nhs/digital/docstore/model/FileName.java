package uk.nhs.digital.docstore.model;

import java.util.Arrays;

public class FileName {
    private final String value;

    public FileName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        var fileNameAndTypes = value.split("\\.");
        var redactedFileName = fileNameAndTypes[0].charAt(0) + "***" + fileNameAndTypes[0].charAt(fileNameAndTypes[0].length() - 1);
        var fileTypes = fileNameAndTypes.length > 1 ? parseFileTypes(fileNameAndTypes) : "";

        return redactedFileName + fileTypes;
    }

    private String parseFileTypes(String[] fileNameAndTypes) {
        var fileTypes = Arrays.asList(fileNameAndTypes).subList(1, fileNameAndTypes.length);
        var stringBuilder = new StringBuilder();

        for (String fileType : fileTypes) {
            stringBuilder.append(".").append(fileType);
        }

        return stringBuilder.toString();
    }
}
