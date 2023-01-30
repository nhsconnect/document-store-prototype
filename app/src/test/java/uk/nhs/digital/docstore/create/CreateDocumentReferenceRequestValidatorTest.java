package uk.nhs.digital.docstore.create;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.util.List;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.NHSDocumentReference;
import uk.nhs.digital.docstore.exceptions.InvalidCodingCodeException;
import uk.nhs.digital.docstore.exceptions.MissingRequiredValueException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedCodingSystemException;
import uk.nhs.digital.docstore.helpers.TestHelpers;

public class CreateDocumentReferenceRequestValidatorTest {
  static CreateDocumentReferenceRequestValidator validator =
      new CreateDocumentReferenceRequestValidator();
  static FhirContext fhirContext = FhirContext.forR4();
  static TestHelpers testHelpers = new TestHelpers();

  @Test
  void doesNotThrowAnExceptionIfTheDocumentReferenceRequestIsValid() throws IOException {
    String validRequestJson =
        testHelpers.getContentFromResource("create/valid-create-document-reference-request.json");
    var jsonParser = fhirContext.newJsonParser();
    var inputDocumentReference =
        jsonParser.parseResource(NHSDocumentReference.class, validRequestJson);

    assertThatNoException().isThrownBy(() -> validator.validate(inputDocumentReference));
  }

  @Test
  void throwsAnExceptionIfACodingSystemInTheDocumentReferenceRequestIsNotValid()
      throws IOException {
    String validRequestJson =
        testHelpers.getContentFromResource("create/valid-create-document-reference-request.json");
    var jsonParser = fhirContext.newJsonParser();
    var inputDocumentReference =
        jsonParser.parseResource(NHSDocumentReference.class, validRequestJson);
    var type =
        new CodeableConcept().setCoding(List.of(new Coding().setCode("1234").setSystem("invalid")));
    inputDocumentReference.setType(type);

    assertThatThrownBy(() -> validator.validate(inputDocumentReference))
        .isExactlyInstanceOf(UnrecognisedCodingSystemException.class);
  }

  @Test
  void throwsAnExceptionIfACodingCodeInTheDocumentReferenceRequestIsNull() throws IOException {
    String validRequestJson =
        testHelpers.getContentFromResource("create/valid-create-document-reference-request.json");
    var jsonParser = fhirContext.newJsonParser();
    var inputDocumentReference =
        jsonParser.parseResource(NHSDocumentReference.class, validRequestJson);
    var type =
        new CodeableConcept()
            .setCoding(List.of(new Coding().setCode(null).setSystem("http://snomed.info/sct")));
    inputDocumentReference.setType(type);

    assertThatThrownBy(() -> validator.validate(inputDocumentReference))
        .isExactlyInstanceOf(MissingRequiredValueException.class);
  }

  @Test
  void throwsAnExceptionIfACodingSystemInTheDocumentReferenceRequestIsNull() throws IOException {
    String validRequestJson =
        testHelpers.getContentFromResource("create/valid-create-document-reference-request.json");
    var jsonParser = fhirContext.newJsonParser();
    var inputDocumentReference =
        jsonParser.parseResource(NHSDocumentReference.class, validRequestJson);
    var type =
        new CodeableConcept().setCoding(List.of(new Coding().setCode("1234").setSystem(null)));
    inputDocumentReference.setType(type);

    assertThatThrownBy(() -> validator.validate(inputDocumentReference))
        .isExactlyInstanceOf(MissingRequiredValueException.class);
  }

  @Test
  void throwsAnExceptionIfTheDescriptionInTheDocumentReferenceRequestIsMissing()
      throws IOException {
    String validRequestJson =
        testHelpers.getContentFromResource(
            "create/create-document-reference-request-without-description.json");
    var jsonParser = fhirContext.newJsonParser();
    var inputDocumentReference =
        jsonParser.parseResource(NHSDocumentReference.class, validRequestJson);
    assertThatThrownBy(() -> validator.validate(inputDocumentReference))
        .isExactlyInstanceOf(MissingRequiredValueException.class);
  }

  @Test
  void throwsAnExceptionIfACodingCodeInTheDocumentReferenceRequestIsNotValid() throws IOException {
    String validRequestJson =
        testHelpers.getContentFromResource("create/valid-create-document-reference-request.json");
    var jsonParser = fhirContext.newJsonParser();
    var inputDocumentReference =
        jsonParser.parseResource(NHSDocumentReference.class, validRequestJson);
    var type =
        new CodeableConcept()
            .setCoding(
                List.of(new Coding().setCode("invalid").setSystem("http://snomed.info/sct")));
    inputDocumentReference.setType(type);
    assertThatThrownBy(() -> validator.validate(inputDocumentReference))
        .isExactlyInstanceOf(InvalidCodingCodeException.class);
  }
}
