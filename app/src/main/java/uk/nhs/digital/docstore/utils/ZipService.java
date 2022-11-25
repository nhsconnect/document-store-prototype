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

public class ZipService {
    private final DocumentStore documentStore;

    public ZipService() {
       var bucketName = System.getenv("DOCUMENT_STORE_BUCKET_NAME");
       documentStore = new DocumentStore(bucketName);
    }

    public ZipService(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    public ByteArrayInputStream zipDocuments(List<DocumentMetadata> documentMetadataList) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        for (DocumentMetadata metadata : documentMetadataList) {
            if (metadata.isDocumentUploaded()){
                zipOutputStream.putNextEntry(new ZipEntry(metadata.getDescription()));

                IOUtils.copy(documentStore.getObjectFromS3(metadata), zipOutputStream);

                zipOutputStream.closeEntry();
            }
        }

        zipOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}