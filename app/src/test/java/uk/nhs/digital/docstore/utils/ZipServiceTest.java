package uk.nhs.digital.docstore.utils;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;

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

        var documentList =  List.of(DocumentBuilder.baseDocumentBuilder().build());

        var result = zipService.zipDocuments(documentList);

        var fileNames = listZipEntryNames(result);

        assertThat(fileNames.size()).isEqualTo(1);
        assertThat(fileNames.get(0)).isEqualTo(documentList.get(0).getDescription().getValue());
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