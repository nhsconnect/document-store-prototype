package uk.nhs.digital.docstore.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.digital.docstore.data.repository.DocumentStore;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.ScanResult;

@ExtendWith(MockitoExtension.class)
class ZipServiceTest {

    @Mock private DocumentStore documentStore;

    @InjectMocks private ZipService zipService;

    @Test
    void shouldZipSpecifiedFilesFromS3() throws IOException {
        var s3Object =
                new S3ObjectInputStream(new ByteArrayInputStream(new byte[10]), new HttpGet());
        when(documentStore.getObjectFromS3(any())).thenReturn(s3Object);

        var documentList =
                List.of(
                        DocumentBuilder.baseDocumentBuilder()
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build());

        var result = zipService.zipDocuments(documentList);

        var fileNames = listZipEntryNames(result);

        assertThat(fileNames.size()).isEqualTo(1);
        assertThat(fileNames.get(0)).isEqualTo(documentList.get(0).getFileName().getValue());
    }

    @Test
    void shouldZipSpecifiedFilesFromS3IfDuplicateFileNamesWithExtension() throws IOException {
        var s3Object =
                new S3ObjectInputStream(new ByteArrayInputStream(new byte[10]), new HttpGet());
        var fileName = new FileName("some-file-name.txt");
        var documentList =
                List.of(
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build());

        when(documentStore.getObjectFromS3(any())).thenReturn(s3Object);

        var result = zipService.zipDocuments(documentList);

        var fileNames = listZipEntryNames(result);

        assertThat(fileNames.size()).isEqualTo(3);
        assertThat(fileNames.get(0)).isEqualTo(fileName.getValue());
        assertThat(fileNames.get(1)).isEqualTo("some-file-name(1).txt");
        assertThat(fileNames.get(2)).isEqualTo("some-file-name(2).txt");
    }

    @Test
    void shouldZipSpecifiedFilesFromS3IfDuplicateFileNamesWithOutExtension() throws IOException {
        var s3Object =
                new S3ObjectInputStream(new ByteArrayInputStream(new byte[10]), new HttpGet());
        var fileName = new FileName("some-file-name");
        var documentList =
                List.of(
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build());

        when(documentStore.getObjectFromS3(any())).thenReturn(s3Object);

        var result = zipService.zipDocuments(documentList);

        var fileNames = listZipEntryNames(result);

        assertThat(fileNames.size()).isEqualTo(3);
        assertThat(fileNames.get(0)).isEqualTo(fileName.getValue());
        assertThat(fileNames.get(1)).isEqualTo("some-file-name(1)");
        assertThat(fileNames.get(2)).isEqualTo("some-file-name(2)");
    }

    @Test
    void shouldZipSpecifiedFilesFromS3IfDuplicateAndIncludesSpecialCharacters() throws IOException {
        var s3Object =
                new S3ObjectInputStream(new ByteArrayInputStream(new byte[10]), new HttpGet());
        var fileName = new FileName("so.me-file-name.txt");
        var documentList =
                List.of(
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(fileName)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build());

        when(documentStore.getObjectFromS3(any())).thenReturn(s3Object);

        var result = zipService.zipDocuments(documentList);

        var fileNames = listZipEntryNames(result);

        assertThat(fileNames.size()).isEqualTo(3);
        assertThat(fileNames.get(0)).isEqualTo(fileName.getValue());
        assertThat(fileNames.get(1)).isEqualTo("so.me-file-name(1).txt");
        assertThat(fileNames.get(2)).isEqualTo("so.me-file-name(2).txt");
    }

    @Test
    void shouldOnlyZipUninfectedFiles() throws IOException {
        var s3Object =
                new S3ObjectInputStream(new ByteArrayInputStream(new byte[10]), new HttpGet());
        var clean1File = new FileName("clean1");
        var clean2File = new FileName("clean2");
        var infectedFile = new FileName("infected");

        var documentList =
                List.of(
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(clean1File)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(clean2File)
                                .withVirusScanResult(ScanResult.CLEAN)
                                .build(),
                        DocumentBuilder.baseDocumentBuilder()
                                .withFileName(infectedFile)
                                .withVirusScanResult(ScanResult.INFECTED)
                                .build());

        when(documentStore.getObjectFromS3(any())).thenReturn(s3Object);

        var result = zipService.zipDocuments(documentList);

        var fileNames = listZipEntryNames(result);

        assertThat(fileNames.size()).isEqualTo(2);
        assertThat(clean1File.getValue()).isEqualTo(fileNames.get(0));
        assertThat(clean2File.getValue()).isEqualTo(fileNames.get(1));
    }

    public ArrayList<Object> listZipEntryNames(ByteArrayInputStream inputStream)
            throws IOException {
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
