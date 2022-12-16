package uk.nhs.digital.docstore.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import uk.nhs.digital.docstore.DocumentStore;
import uk.nhs.digital.docstore.data.entity.DocumentMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.zip.Deflater.DEFLATED;

public class ZipService {
    private final DocumentStore documentStore;

    public ZipService() {
       documentStore = new DocumentStore(System.getenv("S3_ENDPOINT"), "true".equals(System.getenv("S3_USE_PATH_STYLE")));
    }

    public ZipService(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    public ByteArrayInputStream zipDocuments(List<DocumentMetadata> documentMetadataList) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        zipOutputStream.setLevel(DEFLATED);

        for (DocumentMetadata metadata : documentMetadataList) {
            if (metadata.isDocumentUploaded()){
                zipOutputStream.putNextEntry(new ZipEntry(metadata.getDescription()));

                IOUtils.copy(documentStore.getObjectFromS3(DocumentStore.DocumentDescriptor.from(metadata)), zipOutputStream);

                zipOutputStream.closeEntry();
            }
        }

        zipOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
