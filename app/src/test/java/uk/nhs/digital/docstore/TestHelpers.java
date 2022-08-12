package uk.nhs.digital.docstore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestHelpers {
    public String getContentFromResource(String resourcePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }
}
