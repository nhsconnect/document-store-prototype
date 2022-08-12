package uk.nhs.digital.docstore.create;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import uk.nhs.digital.docstore.NHSDocumentReference;
import uk.nhs.digital.docstore.UnrecognisedCodingSystemException;

public class CreateDocumentReferenceRequestValidator {
    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";

    public void validate(NHSDocumentReference documentReference) {
        CodeableConcept documentType = documentReference.getType();
        for (Coding coding : documentType.getCoding()) {
            if (!DOCUMENT_TYPE_CODING_SYSTEM.equals(coding.getSystem())) {
                throw new UnrecognisedCodingSystemException(coding.getSystem());
            }
        }
    }
}
