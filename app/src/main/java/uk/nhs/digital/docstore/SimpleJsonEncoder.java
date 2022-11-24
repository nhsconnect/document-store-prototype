package uk.nhs.digital.docstore;

import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public class SimpleJsonEncoder {
    public String encodeDocumentReferenceToString(DocumentMetadata metadata, URL preSignedUrl) {
        try {
            String docReferenceTemplate = getContentFromResource("templates/document-reference.json");
            String contentProperty = metadata.isDocumentUploaded()
                                     ? getContentFromResource("templates/content-property.json")
                                             .replaceAll("\\$contentType\\$", metadata.getContentType())
                                             .replaceAll("\\$contentUrl\\$", safelyToString(preSignedUrl))
                                     : "";
            return docReferenceTemplate.replaceAll("\\$id\\$", metadata.getId())
                    .replaceAll("\\$nhsNumber\\$", metadata.getNhsNumber())
                    .replaceAll("\\$docStatus\\$", metadata.isDocumentUploaded() ? "final" : "preliminary")
                    .replaceAll("\\$content\\$", contentProperty);
        } catch (IOException e) {
            throw new RuntimeException("Missing template file");
        }
    }

    private <T> String safelyToString(T object) {
        return object != null ? object.toString() : null;
    }

    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream stream = requireNonNull(classLoader.getResourceAsStream(resourcePath))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
