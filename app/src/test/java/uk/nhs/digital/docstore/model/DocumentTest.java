package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentTest {
    @Test
    void shouldReturnDocumentAsString() throws IllFormedPatientDetailsException {
        String referenceId = "123";
        NhsNumber nhsNumber = new NhsNumber("1234567890");
        String contentType = "pdf";
        FileName fileName = new FileName("test.pdf");
        Instant created = Instant.now().minus(30, ChronoUnit.SECONDS);
        Instant indexed = Instant.now();
        List<String> type = List.of("snomed-code");
        DocumentLocation location = new DocumentLocation("s3://test/location");

        var expectedDocumentString = "Document{"
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
        var document = new Document(referenceId, nhsNumber, contentType, true, fileName, created, null, indexed, type, location);

        assertThat(document.toString()).isEqualTo(expectedDocumentString);
    }
}
