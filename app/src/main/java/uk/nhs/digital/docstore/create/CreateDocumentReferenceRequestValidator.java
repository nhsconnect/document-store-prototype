package uk.nhs.digital.docstore.create;

import java.util.List;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import uk.nhs.digital.docstore.NHSDocumentReference;
import uk.nhs.digital.docstore.exceptions.InvalidCodingCodeException;
import uk.nhs.digital.docstore.exceptions.MissingRequiredValueException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedCodingSystemException;

public class CreateDocumentReferenceRequestValidator {
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

    private static final List<String> VALID_CODING_CODES = List.of("22151000087106");

    public void validate(NHSDocumentReference documentReference) {
        CodeableConcept documentType = documentReference.getType();
        var typeCodingIndex = 0;
        for (Coding coding : documentType.getCoding()) {
            if (coding.getSystem() == null) {
                throw new MissingRequiredValueException(
                        String.format("DocumentReference.type.coding[%s].system", typeCodingIndex),
                        "system");
            }

            if (coding.getCode() == null) {
                throw new MissingRequiredValueException(
                        String.format("DocumentReference.type.coding[%s].code", typeCodingIndex),
                        "code");
            }

            if (!DOCUMENT_TYPE_CODING_SYSTEM.equals(coding.getSystem())) {
                throw new UnrecognisedCodingSystemException(coding.getSystem());
            }
            if (coding.getCode() == null || !VALID_CODING_CODES.contains(coding.getCode())) {
                throw new InvalidCodingCodeException("path", coding.getCode());
            }

            typeCodingIndex++;
        }

        String description = documentReference.getDescription();
        if (description == null || description.equals("")) {
            throw new MissingRequiredValueException("DocumentReference.description", "description");
        }
    }
}
