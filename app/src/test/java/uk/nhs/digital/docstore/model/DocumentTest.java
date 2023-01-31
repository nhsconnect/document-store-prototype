package uk.nhs.digital.docstore.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;

public class DocumentTest {
    @Test
    void documentsWithTheSameFieldValuesAreEqual() {
        var document1 = DocumentBuilder.baseDocumentBuilder().build();
        var document2 = DocumentBuilder.baseDocumentBuilder().build();

        assertEquals(document1, document2);
    }

    @Test
    void documentsWithTheDifferentFieldValuesAreNotEqual() throws IllFormedPatientDetailsException {
        var referenceId = "123";
        var nhsNumber = new NhsNumber("1234567890");
        var contentType = "pdf";
        var fileName = new FileName("test.pdf");
        var created = Instant.now().minus(30, ChronoUnit.SECONDS);
        var indexed = Instant.now();
        var type = List.of("snomed-code");

        var documentLocation1 = new DocumentLocation("s3://some/location");
        var documentLocation2 = new DocumentLocation("s3://other/location");

        var document1 =
                new Document(
                        referenceId,
                        nhsNumber,
                        contentType,
                        false,
                        fileName,
                        created,
                        indexed,
                        null,
                        type,
                        documentLocation1);
        var document2 =
                new Document(
                        referenceId,
                        nhsNumber,
                        contentType,
                        false,
                        fileName,
                        created,
                        indexed,
                        null,
                        type,
                        documentLocation2);

        assertNotEquals(document1, document2);
    }

    @Test
    void returnsDocumentAsString() throws IllFormedPatientDetailsException {
        var referenceId = "123";
        var nhsNumber = new NhsNumber("1234567890");
        var contentType = "pdf";
        var fileName = new FileName("test.pdf");
        var created = Instant.now().minus(30, ChronoUnit.SECONDS);
        var indexed = Instant.now();
        var type = List.of("snomed-code");
        var location = new DocumentLocation("s3://test/location");
        var expectedDocumentString =
                "Document{"
                        + "referenceId='"
                        + referenceId
                        + '\''
                        + ", nhsNumber='"
                        + nhsNumber
                        + '\''
                        + ", contentType='"
                        + contentType
                        + '\''
                        + ", uploaded="
                        + true
                        + ", fileName='"
                        + fileName
                        + '\''
                        + ", created='"
                        + created
                        + '\''
                        + ", deleted='"
                        + null
                        + '\''
                        + ", indexed='"
                        + indexed
                        + '\''
                        + ", type="
                        + type
                        + '\''
                        + ", location='"
                        + location
                        + '}';
        var document =
                new Document(
                        referenceId,
                        nhsNumber,
                        contentType,
                        true,
                        fileName,
                        created,
                        null,
                        indexed,
                        type,
                        location);

        assertThat(document.toString()).isEqualTo(expectedDocumentString);
    }
}
