package uk.nhs.digital.docstore.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.model.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.zip.Deflater.DEFLATED;

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

        for (Document document : documentList) {
            if (document.isUploaded()){
                LOGGER.debug("Document ID: "+ document.getReferenceId()+ ", S3 location: "+ document.getLocation());
                zipOutputStream.putNextEntry(new ZipEntry(document.getDescription()));

                IOUtils.copy(documentStore.getObjectFromS3(document.getLocation()), zipOutputStream);

                zipOutputStream.closeEntry();
            }
        }

        zipOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
