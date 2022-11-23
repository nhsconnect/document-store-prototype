package uk.nhs.digital.docstore.utils;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.DocumentMetadata;
import uk.nhs.digital.docstore.DocumentStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZipServiceTest {

    @Mock
    private DocumentStore documentStore;

    @InjectMocks
    private ZipService zipService;

    @Test
    void shouldZipSpecifiedFilesFromS3() throws IOException {
        var s3Object = new S3ObjectInputStream(new ByteArrayInputStream(new byte[10]), new HttpGet());
        when(documentStore.getObjectFromS3(any())).thenReturn(s3Object);

        var metadataList = getDocumentMetadataList();

        var result = zipService.zipDocuments(metadataList);

        var fileNames = listZipEntryNames(result);

        assertThat(fileNames.size()).isEqualTo(1);
        assertThat(fileNames.get(0)).isEqualTo(metadataList.get(0).getDescription());
    }

    private List<DocumentMetadata> getDocumentMetadataList() {
        var metadata = new DocumentMetadata();
        metadata.setDescription("File.pdf");
        metadata.setLocation("location");
        metadata.setDocumentUploaded(true);

        return List.of(metadata);
    }

    public ArrayList<Object> listZipEntryNames(ByteArrayInputStream inputStream) throws IOException {
        var fileNameArray = new ArrayList<>();
        var zipInputStream = new ZipInputStream(inputStream);
        var zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null) {
            fileNameArray.add(zipEntry.getName());
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.closeEntry();

        return fileNameArray;
    }

}