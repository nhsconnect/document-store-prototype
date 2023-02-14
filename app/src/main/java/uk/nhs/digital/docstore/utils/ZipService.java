package uk.nhs.digital.docstore.utils;

import static java.util.zip.Deflater.DEFLATED;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.model.Document;

public class ZipService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipService.class);
    private final DocumentStore documentStore;

    public ZipService() {
        var bucketName = System.getenv("DOCUMENT_STORE_BUCKET_NAME");
        documentStore = new DocumentStore(bucketName);
    }

    public ZipService(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    public ByteArrayInputStream zipDocuments(List<Document> documentList) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        zipOutputStream.setLevel(DEFLATED);

        LOGGER.debug("Zipping documents...");

        var fileNames = new ArrayList<>(List.of());
        var duplicateFileCount = 1;

        for (Document document : documentList) {
            if (document.isUploaded()) {
                LOGGER.debug(
                        "Document ID: "
                                + document.getReferenceId()
                                + ", S3 location: "
                                + document.getLocation());

                if (fileNames.contains(document.getFileName().getValue())) {
                    zipOutputStream.putNextEntry(
                            new ZipEntry(document.getFileName().getValue() + duplicateFileCount));
                    duplicateFileCount++;
                } else {
                    fileNames.add(document.getFileName().getValue());
                    zipOutputStream.putNextEntry(new ZipEntry(document.getFileName().getValue()));
                }

                IOUtils.copy(
                        documentStore.getObjectFromS3(document.getLocation()), zipOutputStream);

                zipOutputStream.closeEntry();
            }
        }
        zipOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
