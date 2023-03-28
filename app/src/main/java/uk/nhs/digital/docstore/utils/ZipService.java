package uk.nhs.digital.docstore.utils;

import static java.util.zip.Deflater.DEFLATED;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.ScanResult;

public class ZipService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipService.class);
    private final DocumentStore documentStore;

    public ZipService(DocumentStore documentStore) {
        this.documentStore = documentStore;
    }

    public ByteArrayInputStream zipDocuments(List<Document> documentList) throws IOException {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        zipOutputStream.setLevel(DEFLATED);

        LOGGER.debug("Zipping documents...");

        var fileNamesToBeZipped = new HashMap<String, Integer>();

        for (Document document : documentList) {
            var fileName = document.getFileName().getValue();
            if (document.getVirusScanResult() == ScanResult.CLEAN) {
                LOGGER.debug(
                        "Document ID: "
                                + document.getReferenceId()
                                + ", S3 location: "
                                + document.getLocation());

                var isDuplicate = fileNamesToBeZipped.get(fileName);

                if (isDuplicate != null) {
                    fileNamesToBeZipped.put(fileName, isDuplicate + 1);
                    var uniqueFileName = createUniqueFileName(document.getFileName(), isDuplicate);
                    zipOutputStream.putNextEntry(new ZipEntry(uniqueFileName));
                } else {
                    fileNamesToBeZipped.put(fileName, 1);
                    zipOutputStream.putNextEntry(new ZipEntry(fileName));
                }

                IOUtils.copy(
                        documentStore.getObjectFromS3(document.getLocation()), zipOutputStream);

                zipOutputStream.closeEntry();
            }
        }
        zipOutputStream.close();
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public String createUniqueFileName(FileName fileName, int duplicateFileCount) {
        return fileName.getBaseName() + "(" + duplicateFileCount + ")" + fileName.getExtension();
    }
}
