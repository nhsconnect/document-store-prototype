package uk.nhs.digital.docstore.create;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import uk.nhs.digital.docstore.NHSDocumentReference;
import uk.nhs.digital.docstore.exceptions.InvalidCodingCodeException;
import uk.nhs.digital.docstore.exceptions.MissingRequiredValueException;
import uk.nhs.digital.docstore.exceptions.UnrecognisedCodingSystemException;

import java.util.List;

public class CreateDocumentReferenceRequestValidator {
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

    private static final List<String> VALID_CODING_CODES = List.of("22151000087106");

    public void validate(NHSDocumentReference documentReference) {
        CodeableConcept documentType = documentReference.getType();
        for (Coding coding : documentType.getCoding()) {
            if (!DOCUMENT_TYPE_CODING_SYSTEM.equals(coding.getSystem())) {
                throw new UnrecognisedCodingSystemException(coding.getSystem());
            }
            if (!VALID_CODING_CODES.contains(coding.getCode())) {
                throw new InvalidCodingCodeException("path", coding.getCode());
            }
        }
        String description = documentReference.getDescription();
        if (description == null) {
            throw new MissingRequiredValueException("DocumentReference.description", "description");
        }
    }
}
