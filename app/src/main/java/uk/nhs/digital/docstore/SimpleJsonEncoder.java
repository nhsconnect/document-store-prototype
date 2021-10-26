package uk.nhs.digital.docstore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class SimpleJsonEncoder {

    public String encodeDocumentReferenceToString(DocumentMetadata metadata, URL presignedUrl) {
        try {
            String docReferenceTemplate = getContentFromResource("DocumentReferenceTemplate.json");
            return docReferenceTemplate.replaceAll("\\$id\\$", metadata.getId())
                    .replaceAll("\\$nhsNumber\\$", metadata.getNhsNumber())
                    .replaceAll("\\$contentType\\$", metadata.getContentType())
                    .replaceAll("\\$contentUrl\\$", presignedUrl.toString());
        } catch (IOException e) {
            throw new RuntimeException("Missing template file");
        }
    }
    private String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
