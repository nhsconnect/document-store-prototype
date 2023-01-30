package uk.nhs.digital.docstore;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hl7.fhir.r4.model.DocumentReference.ReferredDocumentStatus.FINAL;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;

class NHSDocumentReferenceTest {
  private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

  @Test
  void testItDeserializesToADocumentModel()
      throws IllFormedPatientDetailsException, MalformedURLException {
    NhsNumber nhsNumber = new NhsNumber("1234567890");
    String contentType = "pdf";
    String snomedCode = "1244";
    URL presignedUrl = new URL("http://s3.test/object");

    var type =
        new CodeableConcept()
            .setCoding(
                Stream.of(snomedCode)
                    .map(code -> new Coding().setCode(code).setSystem(DOCUMENT_TYPE_CODING_SYSTEM))
                    .collect(toList()));

    String description = "Test Document";
    var created = DateTimeType.now();

    var dto =
        (NHSDocumentReference)
            new NHSDocumentReference()
                .setCreated(created)
                .setNhsNumber(nhsNumber)
                .addContent(
                    new NHSDocumentReference.DocumentReferenceContentComponent()
                        .setAttachment(
                            new Attachment()
                                .setUrl(presignedUrl.toString())
                                .setContentType(contentType)))
                .setType(type)
                .setDocStatus(FINAL)
                .setDescription(description);

    var documentModel = dto.parse();

    assertThat(documentModel.getNhsNumber()).isEqualTo(nhsNumber);
    assertThat(documentModel.getContentType()).isEqualTo(contentType);
    assertThat(documentModel.getFileName().getValue()).isEqualTo(description);
    assertThat(documentModel.getType()).isEqualTo(List.of(snomedCode));
    assertThat(documentModel.isUploaded()).isEqualTo(false);
    assertThat(documentModel.getCreated()).isEqualTo(created.getValue().toInstant());
    assertThat(documentModel.getIndexed()).isNull();
    assertThat(documentModel.getDeleted()).isNull();
  }
}
